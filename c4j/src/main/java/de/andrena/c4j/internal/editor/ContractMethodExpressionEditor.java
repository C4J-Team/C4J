package de.andrena.c4j.internal.editor;

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
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.andrena.c4j.Condition;
import de.andrena.c4j.Configuration.DefaultPreCondition;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.UsageException;
import de.andrena.c4j.internal.compiler.AssignmentExp;
import de.andrena.c4j.internal.compiler.BooleanExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.InvolvedTypeInspector;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.Stackalyzer;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	Set<CtClass> nestedInnerClasses = new HashSet<CtClass>();
	private ContractInfo contract;
	private RootTransformer rootTransformer;
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	private Stackalyzer stackalyzer = new Stackalyzer();
	private List<byte[]> storeDependencies = new ArrayList<byte[]>();
	private List<byte[]> unchangeableStoreDependencies = new ArrayList<byte[]>();
	private UsageException thrownException;

	public UsageException getThrownException() {
		return thrownException;
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
		CtMethod method = methodCall.getMethod();
		if (method.getDeclaringClass().getName().equals(Condition.class.getName())) {
			if (method.getName().equals("old")) {
				handleOldMethodCall(methodCall);
			} else if (method.getName().equals("unchanged")) {
				handleUnchangedMethodCall(methodCall);
			} else if (method.getName().equals("pre") || method.getName().equals("preCondition")) {
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
		if (!rootTransformer.getConfigurationManager().getConfiguration(contract.getTargetClass())
				.isStrengtheningPreConditionAllowed()) {
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
		} catch (UsageException e) {
			thrownException = e;
			return;
		}
		int storeIndex = storeDependencies.size();
		storeDependencies.add(dependencyBytes);
		StaticCallExp oldCall = new StaticCallExp(Evaluator.oldRetrieve, new ValueExp(storeIndex));
		AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
		methodCall.replace(assignmentExp.toStandalone().getCode());
	}

	private void handleUnchangedMethodCall(MethodCall methodCall) throws CannotCompileException, NotFoundException,
			BadBytecode {
		byte[] dependencyBytes;
		try {
			dependencyBytes = stackalyzer.getDependenciesFor(methodCall.where(), methodCall.indexOfBytecode());
		} catch (UsageException e) {
			thrownException = e;
			return;
		}
		int storeIndex = unchangeableStoreDependencies.size();
		unchangeableStoreDependencies.add(dependencyBytes);
		StaticCallExp oldCall = new StaticCallExp(Evaluator.isUnchanged, new StaticCallExp(Evaluator.oldRetrieve,
				new ValueExp(storeIndex)), NestedExp.PROCEED);
		AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
		methodCall.replace(assignmentExp.toStandalone().getCode());
		contract.getMethodsContainingUnchanged().add(methodCall.where().getName() + methodCall.where().getSignature());
	}
}
