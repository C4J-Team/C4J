package de.andrena.c4j.internal.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;

import org.apache.log4j.Logger;

import de.andrena.c4j.Condition;
import de.andrena.c4j.Configuration.DefaultPreCondition;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.ArrayExp;
import de.andrena.c4j.internal.compiler.AssignmentExp;
import de.andrena.c4j.internal.compiler.BooleanExp;
import de.andrena.c4j.internal.compiler.CompareExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.InvolvedTypeInspector;
import de.andrena.c4j.internal.util.ListOrderedSet;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	CtMethod lastMethodCall;
	CtField lastFieldAccess;
	List<CtMember> arrayMembers = new ArrayList<CtMember>();
	private int arrayStartIndex;
	Set<CtClass> nestedInnerClasses = new HashSet<CtClass>();
	private List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
	private ContractInfo contract;
	private RootTransformer rootTransformer;
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	List<NestedExp> unchangeableObjects = new ArrayList<NestedExp>();

	public List<StaticCallExp> getStoreExpressions() {
		return storeExpressions;
	}

	public ContractMethodExpressionEditor(RootTransformer rootTransformer, ContractInfo contract)
			throws NotFoundException {
		this.rootTransformer = rootTransformer;
		this.contract = contract;
	}

	public Set<CtClass> getAndClearNestedInnerClasses() {
		HashSet<CtClass> cachedInnerClasses = new HashSet<CtClass>(nestedInnerClasses);
		nestedInnerClasses.clear();
		return cachedInnerClasses;
	}

	@Override
	public void edit(NewArray newArray) throws CannotCompileException {
		arrayMembers.clear();
		arrayStartIndex = newArray.indexOfBytecode();
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	void editFieldAccess(FieldAccess fieldAccess) throws NotFoundException, CannotCompileException {
		CtField field = fieldAccess.getField();
		if (!field.getDeclaringClass().equals(contract.getContractClass())) {
			lastFieldAccess = field;
			arrayMembers.add(field);
			lastMethodCall = null;
			logger.trace("last field access: " + field.getName());
		}
	}

	@Override
	public void edit(MethodCall methodCall) throws CannotCompileException {
		try {
			editMethodCall(methodCall);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		} catch (BadBytecode e) {
			throw new CannotCompileException(e);
		}
	}

	void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException, BadBytecode {
		CtMethod method = methodCall.getMethod();
		if (involvedTypeInspector.inspect(contract.getTargetClass()).contains(method.getDeclaringClass())) {
			handleTargetMethodCall(methodCall);
		} else if (method.getDeclaringClass().getName().equals(Condition.class.getName())) {
			if (method.getName().equals("old")) {
				handleOldMethodCall(methodCall);
			} else if (method.getName().equals("unchanged")) {
				handleUnchangedMethodCall(methodCall);
			} else if (method.getName().equals("pre")) {
				handlePreConditionMethodCall(methodCall);
			}
		}
	}

	private void handlePreConditionMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtBehavior method = methodCall.where();
		if (contract.getContractClass().equals(method.getDeclaringClass())) {
			ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(contract.getTargetClass());
			if (rootTransformer.getConfigurationManager().getConfiguration(contract.getTargetClass())
					.getDefaultPreCondition() == DefaultPreCondition.TRUE) {
				handleTrueDefaultPreCondition(methodCall, method, involvedTypes);
			} else {
				handleUndefinedDefaultPreCondition(methodCall, method, involvedTypes);
			}
		}
	}

	private void handleTrueDefaultPreCondition(MethodCall methodCall, CtBehavior method,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, CannotCompileException {
		involvedTypes.remove(contract.getTargetClass());
		for (CtClass involvedType : involvedTypes) {
			try {
				involvedType.getDeclaredMethod(method.getName(), method.getParameterTypes());
				preConditionStrengthening(methodCall, method, involvedType);
				return;
			} catch (NotFoundException e) {
			}
		}
	}

	private void handleUndefinedDefaultPreCondition(MethodCall methodCall, CtBehavior method,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, CannotCompileException {
		ListOrderedSet<ContractInfo> contracts = rootTransformer.getContractsForTypes(involvedTypes);
		contracts.remove(contract);
		for (ContractInfo otherContract : contracts) {
			try {
				otherContract.getTargetClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
				preConditionStrengthening(methodCall, method, otherContract.getContractClass());
				return;
			} catch (NotFoundException e) {
			}
		}
	}

	private void preConditionStrengthening(MethodCall methodCall, CtBehavior method, CtClass definingClass)
			throws CannotCompileException, NotFoundException {
		if (!rootTransformer.getConfigurationManager().getConfiguration(contract.getTargetClass())
				.isStrengtheningPreConditionAllowed()) {
			logger.error(("found strengthening pre-condition in " + method.getLongName()
					+ " which is already defined from " + definingClass.getName()) + " - ignoring the pre-condition");
		}
		AssignmentExp replacementExp = new AssignmentExp(NestedExp.RETURN_VALUE, BooleanExp.FALSE);
		replacementExp.toStandalone().replace(methodCall);
	}

	private void handleTargetMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		lastMethodCall = method;
		arrayMembers.add(method);
		lastFieldAccess = null;
		logger.trace("last method call: " + method.getLongName());
	}

	private void handleOldMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		StaticCallExp oldCall = null;
		if (lastFieldAccess != null) {
			storeLastFieldAccess(lastFieldAccess);
			oldCall = new StaticCallExp(Evaluator.oldFieldAccess, new ValueExp(lastFieldAccess.getName()));
		} else if (lastMethodCall != null) {
			if (lastMethodCall.getParameterTypes().length > 0) {
				throw new CannotCompileException("cannot use methods with parameters in old()");
			}
			storeLastMethodCall(lastMethodCall);
			oldCall = new StaticCallExp(Evaluator.oldMethodCall, new ValueExp(lastMethodCall.getName()));
		}
		if (oldCall != null) {
			AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
			methodCall.replace(assignmentExp.toStandalone().getCode());
		}
	}

	private void storeLastMethodCall(CtMethod method) {
		logger.trace("storing method call to " + method);
		storeExpressions.add(new StaticCallExp(Evaluator.storeMethodCall, new ValueExp(method.getName())));
	}

	private void storeLastFieldAccess(CtField field) {
		logger.trace("storing field access to " + field);
		storeExpressions.add(new StaticCallExp(Evaluator.storeFieldAccess, new ValueExp(field.getName())));
	}

	private void handleUnchangedMethodCall(MethodCall methodCall) throws CannotCompileException, NotFoundException,
			BadBytecode {
		logger.trace("beginning to store fields and methods for unchanged");
		BooleanExp conditions = BooleanExp.TRUE;
		for (CtMember arrayMember : arrayMembers) {
			conditions = conditions.and(getReplacementCallForArrayMember(arrayMember));
		}
		addUnchangeableParameterArrayMembers(methodCall.where(), methodCall.indexOfBytecode());
		StandaloneExp replacementCall = new AssignmentExp(NestedExp.RETURN_VALUE, conditions).toStandalone();
		logger.trace("replacement code for unchanged: " + replacementCall.getCode());
		methodCall.replace(replacementCall.getCode());
	}

	private void addUnchangeableParameterArrayMembers(CtBehavior contractBehavior, int maxIndex)
			throws BadBytecode, NotFoundException {
		CodeAttribute ca = contractBehavior.getMethodInfo().getCodeAttribute();
		CodeIterator ci = ca.iterator();
		while (ci.hasNext()) {
			int index = ci.next();
			if (index <= arrayStartIndex) {
				continue;
			}
			if (index >= maxIndex) {
				return;
			}
			int op = ci.byteAt(index);
			if (op == Opcode.ALOAD_1) {
				addUnchangeableParameter(contractBehavior, 1);
			} else if (op == Opcode.ALOAD_2) {
				addUnchangeableParameter(contractBehavior, 2);
			} else if (op == Opcode.ALOAD_3) {
				addUnchangeableParameter(contractBehavior, 3);
			} else if (op == Opcode.ALOAD) {
				addUnchangeableParameter(contractBehavior, ci.byteAt(index + 1));
			}
		}
	}

	private void addUnchangeableParameter(CtBehavior contractBehavior, int parameterIndex) throws NotFoundException {
		if (parameterIndex <= contractBehavior.getParameterTypes().length) {
			unchangeableObjects.add(NestedExp.arg(parameterIndex));
		}
	}

	private BooleanExp getReplacementCallForArrayMember(CtMember arrayMember) throws NotFoundException {
		NestedExp equalExpLeft;
		NestedExp equalExpRight;
		CtClass memberType;
		NestedExp unchangeableObject;
		if (arrayMember instanceof CtField) {
			storeLastFieldAccess((CtField) arrayMember);
			unchangeableObject = NestedExp.field((CtField) arrayMember);
			memberType = ((CtField) arrayMember).getType();
			equalExpLeft = new StaticCallExp(Evaluator.fieldAccess, new ValueExp(arrayMember.getName()));
			equalExpRight = new StaticCallExp(Evaluator.oldFieldAccess, new ValueExp(arrayMember.getName()));
		} else {
			storeLastMethodCall((CtMethod) arrayMember);
			unchangeableObject = NestedExp.THIS.appendCall(arrayMember.getName());
			memberType = ((CtMethod) arrayMember).getReturnType();
			equalExpLeft = new StaticCallExp(Evaluator.methodCall, new ValueExp(arrayMember.getName()), new ArrayExp(
					Class.class), new ArrayExp(Object.class));
			equalExpRight = new StaticCallExp(Evaluator.oldMethodCall, new ValueExp(arrayMember.getName()));
		}
		if (memberType.isPrimitive()) {
			return new CompareExp(equalExpLeft).isEqual(equalExpRight);
		}
		unchangeableObjects.add(unchangeableObject);
		return new CompareExp(equalExpLeft).eq(equalExpRight);
	}

	public List<NestedExp> getUnchangeableObjects() {
		return unchangeableObjects;
	}
}
