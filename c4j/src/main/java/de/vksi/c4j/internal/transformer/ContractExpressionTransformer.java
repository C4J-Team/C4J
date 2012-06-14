package de.vksi.c4j.internal.transformer;

import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.editor.ContractMethodExpressionEditor;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.OldCache;
import de.vksi.c4j.internal.evaluator.PureEvaluator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.TransformationHelper;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	public static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before";
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private TransformationHelper transformationHelper = new TransformationHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(rootTransformer,
				contractInfo, contractBehavior);
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + contractBehavior.getLongName());
		}
		contractBehavior.instrument(expressionEditor);
		if (expressionEditor.getThrownException() != null) {
			throw expressionEditor.getThrownException();
		}
		additionalStoreExpressions(expressionEditor);
		if (expressionEditor.getStoreDependencies().isEmpty()
				&& expressionEditor.getUnchangeableStoreDependencies().isEmpty()) {
			return;
		}
		insertStoreDependencies(contractBehavior, expressionEditor);
	}

	private void insertStoreDependencies(CtBehavior contractBehavior, ContractMethodExpressionEditor expressionEditor)
			throws BadBytecode, CannotCompileException, NotFoundException {
		if (contractBehavior.hasAnnotation(ClassInvariant.class)) {
			insertStoreDependenciesForClassInvariant(contractBehavior, expressionEditor);
		} else {
			insertStoreDependenciesForPostCondition(contractBehavior, expressionEditor);
		}
	}

	private void insertStoreDependenciesForClassInvariant(CtBehavior contractBehavior,
			ContractMethodExpressionEditor expressionEditor) throws BadBytecode, CannotCompileException,
			NotFoundException {
		CtMethod beforeInvariant = CtNewMethod.make(CtClass.voidType,
				contractBehavior.getName() + BEFORE_INVARIANT_METHOD_SUFFIX, new CtClass[0],
				contractBehavior.getExceptionTypes(), null, contractBehavior.getDeclaringClass());
		contractBehavior.getDeclaringClass().addMethod(beforeInvariant);
		transformationHelper.addBehaviorAnnotation(beforeInvariant, rootTransformer.getPool().get(
				BeforeClassInvariant.class.getName()));
		ConstPool constPool = beforeInvariant.getMethodInfo().getConstPool();
		CodeIterator iterator = beforeInvariant.getMethodInfo().getCodeAttribute().iterator();
		insertOldStoreCalls(iterator, expressionEditor.getStoreDependencies(), constPool, false);
		insertOldStoreCalls(iterator, expressionEditor.getUnchangeableStoreDependencies(), constPool, true);
	}

	private void insertStoreDependenciesForPostCondition(CtBehavior contractBehavior,
			ContractMethodExpressionEditor expressionEditor)
			throws BadBytecode {
		ConstPool constPool = contractBehavior.getMethodInfo().getConstPool();
		CodeIterator iterator = contractBehavior.getMethodInfo().getCodeAttribute().iterator();
		int ifBlockLength = insertOldStoreCalls(iterator, expressionEditor.getStoreDependencies(), constPool, false);
		ifBlockLength += insertOldStoreCalls(iterator, expressionEditor.getUnchangeableStoreDependencies(), constPool,
				true);
		insertJump(iterator, ifBlockLength, constPool);
	}

	private void insertJump(CodeIterator iterator, int ifBlockLength, ConstPool constPool) throws BadBytecode {
		int jumpLength = ifBlockLength + 3;
		byte[] ifBytes = new byte[6];
		ifBytes[0] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, ifBytes, 1, Evaluator.isBefore, "()Z");
		ifBytes[3] = (byte) Opcode.IFEQ;
		ifBytes[4] = (byte) (jumpLength >> 8);
		ifBytes[5] = (byte) jumpLength;
		iterator.insert(0, ifBytes);
	}

	private int insertOldStoreCalls(CodeIterator iterator, List<byte[]> storeDependencies, ConstPool constPool,
			boolean isUnchangeable)
			throws BadBytecode {
		int ifBlockLength = 0;
		byte[] oldStoreBytes = getOldStoreBytes(constPool);
		for (int i = 0; i < storeDependencies.size(); i++) {
			byte[] iloadBytes = new byte[2];
			iloadBytes[0] = Opcode.BIPUSH;
			iloadBytes[1] = (byte) i;
			iterator.insert(iloadBytes);
			byte[] storeDependency = storeDependencies.get(i);
			iterator.insert(storeDependency);
			if (isUnchangeable) {
				byte[] registerUnchangeableBytes = getRegisterUnchangeableBytes(constPool);
				iterator.insert(registerUnchangeableBytes);
				ifBlockLength += registerUnchangeableBytes.length;
			}
			iterator.insert(oldStoreBytes);
			ifBlockLength += storeDependency.length + oldStoreBytes.length + iloadBytes.length;
		}
		return ifBlockLength;
	}

	private byte[] getRegisterUnchangeableBytes(ConstPool constPool) {
		byte[] registerUnchangeableBytes = new byte[4];
		registerUnchangeableBytes[0] = (byte) Opcode.DUP;
		registerUnchangeableBytes[1] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, registerUnchangeableBytes, 2,
				PureEvaluator.registerUnchangeable,
				"(Ljava/lang/Object;)V");
		return registerUnchangeableBytes;
	}

	private byte[] getOldStoreBytes(ConstPool constPool) {
		byte[] oldStoreBytes = new byte[3];
		oldStoreBytes[0] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, oldStoreBytes, 1, OldCache.oldStore, "(ILjava/lang/Object;)V");
		return oldStoreBytes;
	}

	private void additionalStoreExpressions(ContractMethodExpressionEditor expressionEditor) throws Exception {
		for (CtClass nestedContractClass : expressionEditor.getAndClearNestedInnerClasses()) {
			for (CtBehavior nestedBehavior : nestedContractClass.getDeclaredBehaviors()) {
				nestedBehavior.instrument(expressionEditor);
			}
			additionalStoreExpressions(expressionEditor);
		}
	}

}
