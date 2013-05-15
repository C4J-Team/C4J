package de.vksi.c4j.internal.editor;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredMethod;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getField;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.vksi.c4j.Condition;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.configuration.DefaultPreconditionType;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.types.ListOrderedSet;
import de.vksi.c4j.internal.util.InvolvedTypeInspector;

public class ContractMethodConditionEditor extends ContractMethodEditor {
	private Logger logger = Logger.getLogger(getClass());
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	private boolean preConditionAvailable;
	private boolean postConditionAvailable;

	public boolean isPostConditionAvailable() {
		return postConditionAvailable;
	}

	public ContractMethodConditionEditor(ContractInfo contract) throws NotFoundException {
		super(contract);
	}

	@Override
	protected void handleMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtClass methodClass = methodCall.getMethod().getDeclaringClass();
		if (methodClass.getName().equals(Condition.class.getName())) {
			handleConditionCall(methodCall);
		}
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		handleRemovedStaticFieldAccess(fieldAccess);
	}

	private void handleRemovedStaticFieldAccess(FieldAccess fieldAccess) throws CannotCompileException {
		if (!fieldAccess.isStatic() || !fieldAccess.getClassName().equals(getContract().getContractClass().getName())) {
			return;
		}
		if (getField(getContract().getContractClass(), fieldAccess.getFieldName()) != null) {
			return;
		}
		CtField targetField = getField(getContract().getTargetClass(), fieldAccess.getFieldName());
		if (targetField == null) {
			return;
		}
		if (fieldAccess.isReader()) {
			new AssignmentExp(NestedExp.RETURN_VALUE, new StaticCallExp(targetField)).replace(fieldAccess);
		} else {
			new AssignmentExp(new StaticCallExp(targetField), NestedExp.arg(1)).replace(fieldAccess);
		}
	}

	void handleConditionCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		if (methodCall.getMethod().getName().equals("preCondition")) {
			handlePreConditionMethodCall(methodCall);
		} else if (methodCall.getMethod().getName().equals("postCondition")) {
			handlePostConditionMethodCall(methodCall);
		}
	}

	private void handlePostConditionMethodCall(MethodCall methodCall) {
		postConditionAvailable = true;
	}

	private void handlePreConditionMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtBehavior method = methodCall.where();
		if (getContract().getContractClass().equals(method.getDeclaringClass())) {
			ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(getContract().getTargetClass());
			if (XmlConfigurationManager.INSTANCE.getConfiguration(getContract().getTargetClass())
					.getDefaultPrecondition() == DefaultPreconditionType.TRUE) {
				handleTrueDefaultPreCondition(methodCall, method, involvedTypes);
			} else {
				handleUndefinedDefaultPreCondition(methodCall, method, involvedTypes);
			}
		}
	}

	private void handleTrueDefaultPreCondition(MethodCall methodCall, CtBehavior method,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, CannotCompileException {
		involvedTypes.remove(getContract().getTargetClass());
		for (CtClass involvedType : involvedTypes) {
			if (getDeclaredMethod(involvedType, method.getName(), method.getParameterTypes()) != null) {
				preConditionStrengthening(methodCall, method, involvedType);
				return;
			}
		}
		preConditionAvailable = true;
	}

	private void handleUndefinedDefaultPreCondition(MethodCall methodCall, CtBehavior method,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, CannotCompileException {
		ListOrderedSet<ContractInfo> contracts = ContractRegistry.INSTANCE.getContractsForTypes(involvedTypes,
				getContract().getTargetClass());
		contracts.remove(getContract());
		for (ContractInfo otherContract : contracts) {
			if (getDeclaredMethod(otherContract.getTargetClass(), method.getName(), method.getParameterTypes()) != null) {
				preConditionStrengthening(methodCall, method, otherContract.getContractClass());
				return;
			}
		}
		preConditionAvailable = true;
	}

	private void preConditionStrengthening(MethodCall methodCall, CtBehavior method, CtClass definingClass)
			throws CannotCompileException {
		logger.error(("Found strengthening pre-condition in " + method.getLongName()
				+ " which is already defined from " + definingClass.getName())
				+ " - ignoring the pre-condition.");
		new AssignmentExp(NestedExp.RETURN_VALUE, BooleanExp.FALSE).replace(methodCall);
	}

	public boolean hasPreCondition() {
		return preConditionAvailable;
	}
}
