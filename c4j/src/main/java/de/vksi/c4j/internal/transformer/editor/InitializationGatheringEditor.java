package de.vksi.c4j.internal.transformer.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.MethodCall;
import de.vksi.c4j.Condition;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.runtime.MaxTimeCache;
import de.vksi.c4j.internal.runtime.OldCache;
import de.vksi.c4j.internal.runtime.UnchangedCache;
import de.vksi.c4j.internal.transformer.util.Stackalyzer;

public class InitializationGatheringEditor extends ContractMethodEditor {

	private Stackalyzer stackalyzer = new Stackalyzer();
	private List<StoreDependency> storeDependencies = new ArrayList<StoreDependency>();
	private StandaloneExp preConditionExp = new EmptyExp();
	private final AtomicInteger storeIndex;
	private boolean containsUnchanged;
	private Set<CtMethod> contractMethodCalls = new HashSet<CtMethod>();

	public InitializationGatheringEditor(AtomicInteger storeIndex, ContractInfo contract) {
		super(contract);
		this.storeIndex = storeIndex;
	}

	@Override
	protected void handleMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException,
			BadBytecode {
		CtMethod method = methodCall.getMethod();
		String methodClass = method.getDeclaringClass().getName();
		if (methodClass.equals(Condition.class.getName())) {
			if (method.getName().equals("old")) {
				handleOldMethodCall(methodCall);
			} else if (method.getName().equals("unchanged")) {
				handleUnchangedMethodCall(methodCall);
			} else if (method.getName().equals("maxTime")) {
				handleMaxTimeMethodCall(methodCall);
			}
		}
		if (method.getDeclaringClass().equals(getContract().getContractClass())) {
			contractMethodCalls.add(methodCall.getMethod());
		}
	}

	private void handleMaxTimeMethodCall(MethodCall methodCall) {
		preConditionExp = preConditionExp.append(new StaticCallExp(MaxTimeCache.setStartTime));
	}

	private byte[] addStoreDependency(MethodCall methodCall, int newStoreIndex, boolean unchangeable)
			throws BadBytecode, NotFoundException {
		try {
			byte[] dependencyBytes = stackalyzer.getDependenciesFor(methodCall.where(), methodCall.indexOfBytecode());
			storeDependencies.add(new StoreDependency(dependencyBytes, unchangeable, newStoreIndex));
			return dependencyBytes;
		} catch (UsageError e) {
			setThrownException(e);
			return null;
		}
	}

	public boolean hasStoreDependencies() {
		return !getStoreDependencies().isEmpty();
	}

	public boolean hasPreDependencies() {
		return hasStoreDependencies() || !getPreConditionExp().isEmpty();
	}

	private void handleOldMethodCall(MethodCall methodCall) throws NotFoundException, BadBytecode,
			CannotCompileException {
		int newStoreIndex = storeIndex.getAndIncrement();
		StaticCallExp oldCall = new StaticCallExp(OldCache.oldRetrieve, new ValueExp(getContract().getContractClass()),
				new ValueExp(newStoreIndex));
		byte[] dependencyBytes = addStoreDependency(methodCall, newStoreIndex, false);
		if (dependencyBytes == null) {
			return;
		}
		eraseOriginalCall(methodCall, dependencyBytes.length);
		new AssignmentExp(NestedExp.RETURN_VALUE, oldCall).replace(methodCall);
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
		int newStoreIndex = storeIndex.getAndIncrement();
		StaticCallExp oldCall = new StaticCallExp(UnchangedCache.isUnchanged, new StaticCallExp(OldCache.oldRetrieve,
				new ValueExp(getContract().getContractClass()), new ValueExp(newStoreIndex)), NestedExp.PROCEED);
		byte[] dependencyBytes = addStoreDependency(methodCall, newStoreIndex, true);
		if (dependencyBytes == null) {
			return;
		}
		new AssignmentExp(NestedExp.RETURN_VALUE, oldCall).replace(methodCall);
		containsUnchanged = true;
	}

	public StandaloneExp getPreConditionExp() {
		return preConditionExp;
	}

	public List<StoreDependency> getStoreDependencies() {
		return storeDependencies;
	}

	public boolean containsUnchanged() {
		return containsUnchanged;
	}

	public Set<CtMethod> getContractMethodCalls() {
		return contractMethodCalls;
	}

}
