package de.andrena.next.internal.editor;

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
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;

import org.apache.log4j.Logger;

import de.andrena.next.Condition;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.AssignmentExp;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.CompareExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.EmptyExp;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	CtMethod lastMethodCall;
	CtField lastFieldAccess;
	List<CtMember> arrayMembers = new ArrayList<CtMember>();
	Set<CtClass> nestedInnerClasses = new HashSet<CtClass>();
	private List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
	private ContractInfo contract;
	private CtBehavior contractBehavior;

	public List<StaticCallExp> getStoreExpressions() {
		return storeExpressions;
	}

	public ContractMethodExpressionEditor(ContractInfo contract, CtBehavior contractBehavior) throws NotFoundException {
		this.contract = contract;
		this.contractBehavior = contractBehavior;
	}

	public Set<CtClass> getAndClearNestedInnerClasses() {
		HashSet<CtClass> cachedInnerClasses = new HashSet<CtClass>(nestedInnerClasses);
		nestedInnerClasses.clear();
		return cachedInnerClasses;
	}

	@Override
	public void edit(NewArray newArray) throws CannotCompileException {
		arrayMembers.clear();
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
		lastFieldAccess = field;
		arrayMembers.add(field);
		lastMethodCall = null;
		logger.info("last field access: " + field.getName());
		if (fieldAccess.isWriter() && !contract.getAllContractClasses().contains(field.getDeclaringClass())) {
			throw new CannotCompileException("illegal write access on field '" + field.getName() + "'.");
		}
		if (!fieldAccess.isStatic() && field.getDeclaringClass().equals(contract.getTargetClass())) {
			CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.fieldAccess, new ValueExp(field
					.getName())));
			AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
			fieldAccess.replace(assignment.toStandalone().getCode());
		}
	}

	@Override
	public void edit(MethodCall methodCall) throws CannotCompileException {
		try {
			editMethodCall(methodCall);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		if (method.getDeclaringClass().equals(contract.getTargetClass())
				|| method.getDeclaringClass().equals(contract.getContractClass())) {
			try {
				contract.getTargetClass().getMethod(method.getName(), method.getSignature());
			} catch (NotFoundException e) {
				return;
			}
			handleTargetMethodCall(methodCall);
		} else if (method.getDeclaringClass().getName().equals(Condition.class.getName())) {
			if (method.getName().equals("old")) {
				handleOldMethodCall(methodCall);
			} else if (method.getName().equals("unchanged")) {
				handleUnchangedMethodCall(methodCall);
			}
		}
	}

	private void handleTargetMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		lastMethodCall = method;
		arrayMembers.add(method);
		lastFieldAccess = null;
		logger.info("last method call: " + lastMethodCall);
		logger.info("replacing call to " + methodCall.getClassName() + "." + methodCall.getMethodName());
		CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.methodCall, new ValueExp(methodCall
				.getMethodName()), ArrayExp.forParamTypes(method), ArrayExp.forArgs(method)));
		AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
		String code = assignment.toStandalone().getCode();
		logger.info("replacement code: " + code);
		methodCall.replace(code);
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
		logger.info("storing method call to " + method);
		storeExpressions.add(new StaticCallExp(Evaluator.storeMethodCall, new ValueExp(method.getName())));
	}

	private void storeLastFieldAccess(CtField field) {
		logger.info("storing field access to " + field);
		storeExpressions.add(new StaticCallExp(Evaluator.storeFieldAccess, new ValueExp(field.getName())));
	}

	private void handleUnchangedMethodCall(MethodCall methodCall) throws CannotCompileException, NotFoundException {
		logger.info("beginning to store fields and methods for unchanged");
		StandaloneExp replacementCall = new EmptyExp();
		for (CtMember arrayMember : arrayMembers) {
			replacementCall = replacementCall.append(getReplacementCallForArrayMember(arrayMember));
		}
		logger.info("replacement code for unchanged: " + replacementCall.getCode());
		methodCall.replace(replacementCall.getCode());
	}

	private StandaloneExp getReplacementCallForArrayMember(CtMember arrayMember) {
		NestedExp equalExpLeft;
		NestedExp equalExpRight;
		if (arrayMember instanceof CtField) {
			storeLastFieldAccess((CtField) arrayMember);
			equalExpLeft = new StaticCallExp(Evaluator.fieldAccess, new ValueExp(arrayMember.getName()));
			equalExpRight = new StaticCallExp(Evaluator.oldFieldAccess, new ValueExp(arrayMember.getName()));
		} else {
			storeLastMethodCall((CtMethod) arrayMember);
			equalExpLeft = new StaticCallExp(Evaluator.methodCall, new ValueExp(arrayMember.getName()), new ArrayExp(
					Class.class), new ArrayExp(Object.class));
			equalExpRight = new StaticCallExp(Evaluator.oldMethodCall, new ValueExp(arrayMember.getName()));
		}
		IfExp condition = new IfExp(new CompareExp(equalExpLeft).isNotEqual(equalExpRight));
		condition.addIfBody(new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class,
				new ValueExp("the value from member " + arrayMember.getName()
						+ " was changed although being declared unchanged in " + contractBehavior.getLongName())))));
		return condition;
	}
}
