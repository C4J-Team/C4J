package de.vksi.c4j.internal.editor;

import static de.vksi.c4j.internal.util.ReflectionHelper.getMethod;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public abstract class ContractMethodEditor extends ExprEditor {
	private UsageError thrownException;
	private ContractInfo contract;

	public ContractInfo getContract() {
		return contract;
	}

	public ContractMethodEditor(ContractInfo contract) {
		this.contract = contract;
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
		if (removedContractMethodCall(methodCall)) {
			return;
		}
		handleMethodCall(methodCall);
	}

	protected abstract void handleMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException,
			BadBytecode;

	boolean removedContractMethodCall(MethodCall methodCall) throws CannotCompileException {
		if (!methodCall.getClassName().equals(contract.getContractClass().getName())) {
			return false;
		}
		CtMethod targetMethod = getMethod(contract.getTargetClass(), methodCall.getMethodName(), methodCall
				.getSignature());
		if (targetMethod == null) {
			return false;
		}
		if (!Modifier.isStatic(targetMethod.getModifiers())) {
			thrownException = new UsageError("Cannot call contract method " + methodCall.getMethodName()
					+ " from contract method " + methodCall.where().getLongName() + ".");
			return true;
		}
		redirectStaticMethodCallToTargetClass(methodCall, targetMethod);
		return true;
	}

	public UsageError getThrownException() {
		return thrownException;
	}

	protected void setThrownException(UsageError thrownException) {
		this.thrownException = thrownException;
	}

	private void redirectStaticMethodCallToTargetClass(MethodCall methodCall, CtMethod targetMethod)
			throws CannotCompileException {
		new AssignmentExp(NestedExp.RETURN_VALUE, new StaticCallExp(targetMethod, NestedExp.ALL_ARGS))
				.replace(methodCall);
	}

}