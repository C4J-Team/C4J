package de.vksi.c4j.internal.transformer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.editor.ContractMethodExpressionEditor;
import de.vksi.c4j.internal.editor.StoreDependency;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.OldCache;
import de.vksi.c4j.internal.evaluator.PureEvaluator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.TransformationHelper;

public class ContractExpressionTransformer extends AbstractContractClassTransformer {

	public static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before";
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private TransformationHelper transformationHelper = new TransformationHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		AtomicInteger storeIndex = new AtomicInteger();
		for (CtBehavior contractBehavior : currentContractClass.getDeclaredBehaviors()) {
			if (logger.isTraceEnabled()) {
				logger.trace("transforming behavior " + contractBehavior.getLongName());
			}
			transform(contractInfo, contractBehavior, storeIndex);
		}
	}

	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior, AtomicInteger storeIndex)
			throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(rootTransformer,
				contractInfo, storeIndex);
		contractBehavior.instrument(expressionEditor);
		if (expressionEditor.getThrownException() != null) {
			throw expressionEditor.getThrownException();
		}
		if (expressionEditor.hasStoreDependencies() || !expressionEditor.getPreConditionExp().isEmpty()) {
			insertStoreDependencies(contractBehavior, expressionEditor);
		}
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
		CtMethod beforeInvariant = CtNewMethod.make(CtClass.voidType, contractBehavior.getName()
				+ BEFORE_INVARIANT_METHOD_SUFFIX, new CtClass[0], contractBehavior.getExceptionTypes(), null,
				contractBehavior.getDeclaringClass());
		contractBehavior.getDeclaringClass().addMethod(beforeInvariant);
		transformationHelper.addBehaviorAnnotation(beforeInvariant, rootTransformer.getPool().get(
				BeforeClassInvariant.class.getName()));
		insertIntoBeforeInvariant(expressionEditor, beforeInvariant, contractBehavior.getDeclaringClass());
	}

	private void insertIntoBeforeInvariant(ContractMethodExpressionEditor expressionEditor, CtMethod beforeInvariant,
			CtClass contractClass) throws CannotCompileException, BadBytecode {
		if (!expressionEditor.getPreConditionExp().isEmpty()) {
			expressionEditor.getPreConditionExp().insertBefore(beforeInvariant);
		}
		if (expressionEditor.hasStoreDependencies()) {
			ConstPool constPool = beforeInvariant.getMethodInfo().getConstPool();
			CodeAttribute attribute = beforeInvariant.getMethodInfo().getCodeAttribute();
			insertOldStoreCalls(attribute, expressionEditor.getStoreDependencies(), constPool, contractClass);
			attribute.computeMaxStack();
		}
	}

	private void insertStoreDependenciesForPostCondition(CtBehavior contractBehavior,
			ContractMethodExpressionEditor expressionEditor) throws BadBytecode, CannotCompileException {
		if (!expressionEditor.getPreConditionExp().isEmpty()) {
			IfExp isBeforeCondition = new IfExp(new StaticCallExp(Evaluator.isBefore));
			isBeforeCondition.addIfBody(expressionEditor.getPreConditionExp());
			isBeforeCondition.insertBefore(contractBehavior);
		}
		if (expressionEditor.hasStoreDependencies()) {
			ConstPool constPool = contractBehavior.getMethodInfo().getConstPool();
			CodeAttribute attribute = contractBehavior.getMethodInfo().getCodeAttribute();
			int ifBlockLength = insertOldStoreCalls(attribute, expressionEditor.getStoreDependencies(), constPool,
					contractBehavior.getDeclaringClass());
			insertJump(attribute.iterator(), ifBlockLength, constPool);
		}
	}

	private void insertJump(CodeIterator iterator, int ifBlockLength, ConstPool constPool) throws BadBytecode {
		int jumpLength = ifBlockLength + 3;
		byte[] ifBytes = new byte[6];
		ifBytes[0] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, ifBytes, 1, Evaluator.isBefore, "()Z");
		ifBytes[3] = (byte) Opcode.IFEQ;
		ifBytes[4] = (byte) (jumpLength >> 8);
		ifBytes[5] = (byte) jumpLength;
		iterator.insertEx(0, ifBytes);
	}

	private int insertOldStoreCalls(CodeAttribute attribute, List<StoreDependency> storeDependencies,
			ConstPool constPool, CtClass contractClass) throws BadBytecode {
		CodeIterator iterator = attribute.iterator();
		int ifBlockLength = 0;
		byte[] oldStoreBytes = getOldStoreBytes(constPool, OldCache.oldStore);
		byte[] oldStoreExceptionBytes = getOldStoreBytes(constPool, OldCache.oldStoreException);
		byte[] contractClassBytes = getContractClassBytes(constPool, contractClass);
		for (StoreDependency storeDependency : storeDependencies) {
			int startIndex = iterator.insert(storeDependency.getDependency());
			if (storeDependency.isUnchangeable()) {
				byte[] registerUnchangeableBytes = getRegisterUnchangeableBytes(constPool);
				iterator.insert(registerUnchangeableBytes);
			}
			byte[] iloadBytes = getIloadBytes(storeDependency.getIndex());
			iterator.insert(contractClassBytes);
			iterator.insert(iloadBytes);
			iterator.insert(oldStoreBytes);
			byte[] gotoBytes = new byte[3];
			gotoBytes[0] = (byte) Opcode.GOTO;
			int gotoTarget = gotoBytes.length + contractClassBytes.length + iloadBytes.length + oldStoreBytes.length;
			int gotoIndex = iterator.insert(gotoBytes);
			iterator.insert(contractClassBytes);
			iterator.insert(iloadBytes);
			iterator.insert(oldStoreExceptionBytes);
			iterator.write(new byte[] { (byte) (gotoTarget >> 8), (byte) gotoTarget }, gotoIndex + 1);
			int tryLength = gotoIndex - startIndex;
			ifBlockLength += tryLength + gotoBytes.length + contractClassBytes.length + iloadBytes.length
					+ oldStoreExceptionBytes.length;
			int endIndex = startIndex + tryLength;
			attribute.getExceptionTable().add(startIndex, endIndex, endIndex + gotoBytes.length, 0);
		}
		contractClass.debugWriteFile("tmp");
		return ifBlockLength;
	}

	private byte[] getIloadBytes(int i) {
		byte[] iloadBytes = new byte[2];
		iloadBytes[0] = Opcode.BIPUSH;
		iloadBytes[1] = (byte) i;
		return iloadBytes;
	}

	private byte[] getRegisterUnchangeableBytes(ConstPool constPool) {
		byte[] registerUnchangeableBytes = new byte[4];
		registerUnchangeableBytes[0] = (byte) Opcode.DUP;
		registerUnchangeableBytes[1] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, registerUnchangeableBytes, 2,
				PureEvaluator.registerUnchangeable, "(Ljava/lang/Object;)V");
		return registerUnchangeableBytes;
	}

	private byte[] getContractClassBytes(ConstPool constPool, CtClass contractClass) {
		byte[] contractClassBytes = new byte[3];
		contractClassBytes[0] = (byte) Opcode.LDC_W;
		transformationHelper.setClassIndex(constPool, contractClassBytes, 1, contractClass);
		return contractClassBytes;
	}

	private byte[] getOldStoreBytes(ConstPool constPool, StaticCall oldStoreCall) {
		byte[] oldStoreBytes = new byte[3];
		oldStoreBytes[0] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(constPool, oldStoreBytes, 1, oldStoreCall, OldCache.oldStoreDescriptor);
		return oldStoreBytes;
	}

}
