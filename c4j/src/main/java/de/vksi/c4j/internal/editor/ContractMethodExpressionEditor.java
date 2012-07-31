package de.vksi.c4j.internal.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.vksi.c4j.Condition;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.configuration.DefaultPreconditionType;
import de.vksi.c4j.internal.evaluator.MaxTimeCache;
import de.vksi.c4j.internal.evaluator.OldCache;
import de.vksi.c4j.internal.evaluator.UnchangedCache;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.InvolvedTypeInspector;
import de.vksi.c4j.internal.util.ListOrderedSet;
import de.vksi.c4j.internal.util.Stackalyzer;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	Set<CtClass> nestedInnerClasses = new HashSet<CtClass>();
	private ContractInfo contract;
	private RootTransformer rootTransformer;
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	private Stackalyzer stackalyzer = new Stackalyzer();
	private List<byte[]> storeDependencies = new ArrayList<byte[]>();
	private List<byte[]> unchangeableStoreDependencies = new ArrayList<byte[]>();
	private StandaloneExp preConditionExp = new EmptyExp();
	private UsageError thrownException;

	public UsageError getThrownException() {
		return thrownException;
	}

	public StandaloneExp getPreConditionExp() {
		return preConditionExp;
	}

	public List<byte[]> getStoreDependencies() {
		return storeDependencies;
	}

	public List<byte[]> getUnchangeableStoreDependencies() {
		return unchangeableStoreDependencies;
	}

	public ContractMethodExpressionEditor(RootTransformer rootTransformer, ContractInfo contract,
			CtBehavior contractBehavior)
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
		if (invalidContractMethodCall(methodCall)) {
			thrownException = new UsageError("Cannot call contract method " + methodCall.getMethodName()
					+ " from contract method " + methodCall.where().getLongName() + ".");
			return;
		}
		CtMethod method = methodCall.getMethod();
		if (method.getDeclaringClass().getName().equals(Condition.class.getName())) {
			if (method.getName().equals("old")) {
				handleOldMethodCall(methodCall);
			} else if (method.getName().equals("unchanged")) {
				handleUnchangedMethodCall(methodCall);
			} else if (method.getName().equals("preCondition")) {
				handlePreConditionMethodCall(methodCall);
			} else if (method.getName().equals("maxTime")) {
				handleMaxTimeMethodCall(methodCall);
			}
		}
	}

	private void handleMaxTimeMethodCall(MethodCall methodCall) {
		preConditionExp = preConditionExp.append(new StaticCallExp(MaxTimeCache.setStartTime));
	}

	private boolean invalidContractMethodCall(MethodCall methodCall) {
		if (!methodCall.getClassName().equals(contract.getContractClass().getName())) {
			return false;
		}
		CtMethod method;
		try {
			method = methodCall.getMethod();
		} catch (NotFoundException e) {
			return true;
		}
		try {
			contract.getTargetClass().getMethod(method.getName(), method.getSignature());
		} catch (NotFoundException e) {
			return false;
		}
		return true;
	}

	private void handlePreConditionMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtBehavior method = methodCall.where();
		if (contract.getContractClass().equals(method.getDeclaringClass())) {
			ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(contract.getTargetClass());
			if (rootTransformer.getXmlConfiguration().getConfiguration(contract.getTargetClass())
					.getDefaultPrecondition() == DefaultPreconditionType.TRUE) {
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
		ListOrderedSet<ContractInfo> contracts = rootTransformer.getContractsForTypes(involvedTypes, contract
				.getTargetClass());
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
		if (!rootTransformer.getXmlConfiguration().getConfiguration(contract.getTargetClass())
				.isStrengtheningPreconditionsAllowed()) {
			logger.error(("Found strengthening pre-condition in " + method.getLongName()
					+ " which is already defined from " + definingClass.getName()) + " - ignoring the pre-condition.");
		}
		AssignmentExp replacementExp = new AssignmentExp(NestedExp.RETURN_VALUE, BooleanExp.FALSE);
		replacementExp.toStandalone().replace(methodCall);
	}

	private void handleOldMethodCall(MethodCall methodCall) throws NotFoundException, BadBytecode,
			CannotCompileException {
		byte[] dependencyBytes;
		try {
			dependencyBytes = stackalyzer.getDependenciesFor(methodCall.where(), methodCall.indexOfBytecode());
		} catch (UsageError e) {
			thrownException = e;
			return;
		}
		int storeIndex = storeDependencies.size();
		storeDependencies.add(dependencyBytes);
		eraseOriginalCall(methodCall, dependencyBytes.length);
		StaticCallExp oldCall = new StaticCallExp(OldCache.oldRetrieve, new ValueExp(storeIndex));
		AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
		methodCall.replace(assignmentExp.toStandalone().getCode());
	}

	private void eraseOriginalCall(MethodCall methodCall, int length) {
		CodeIterator iterator = methodCall.where().getMethodInfo().getCodeAttribute().iterator();
		int beginIndex = methodCall.indexOfBytecode() - length;
		iterator.writeByte(Opcode.ACONST_NULL, beginIndex);
		for (int i = beginIndex + 1; i < methodCall.indexOfBytecode(); i++) {
			iterator.writeByte(Opcode.NOP, i);
		}
	}

	private void handleUnchangedMethodCall(MethodCall methodCall) throws CannotCompileException, NotFoundException,
			BadBytecode {
		byte[] dependencyBytes;
		try {
			dependencyBytes = stackalyzer.getDependenciesFor(methodCall.where(), methodCall.indexOfBytecode());
		} catch (UsageError e) {
			thrownException = e;
			return;
		}
		int storeIndex = unchangeableStoreDependencies.size();
		unchangeableStoreDependencies.add(dependencyBytes);
		StaticCallExp oldCall = new StaticCallExp(UnchangedCache.isUnchanged, new StaticCallExp(OldCache.oldRetrieve,
				new ValueExp(storeIndex)), NestedExp.PROCEED);
		AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
		methodCall.replace(assignmentExp.toStandalone().getCode());
		contract.getMethodsContainingUnchanged().add(methodCall.where().getName() + methodCall.where().getSignature());
	}

	public boolean hasStoreDependencies() {
		return !getStoreDependencies().isEmpty() || !getUnchangeableStoreDependencies().isEmpty();
	}
}
