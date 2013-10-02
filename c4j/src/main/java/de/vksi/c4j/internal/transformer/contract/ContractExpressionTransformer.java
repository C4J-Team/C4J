package de.vksi.c4j.internal.transformer.contract;

import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isClassInvariant;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.makeBeforeInvariantHelperMethodName;
import static de.vksi.c4j.internal.transformer.util.TransformationHelper.addBehaviorAnnotation;
import static de.vksi.c4j.internal.transformer.util.TransformationHelper.setMethodIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.contracts.BeforeClassInvariant;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.runtime.Evaluator;
import de.vksi.c4j.internal.transformer.editor.ContractMethodConditionEditor;
import de.vksi.c4j.internal.transformer.editor.InitializationGatheringEditor;

public class ContractExpressionTransformer extends AbstractContractClassTransformer {
	private static final Logger LOGGER = Logger.getLogger(ContractExpressionTransformer.class);

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		AtomicInteger storeIndex = new AtomicInteger();
		Map<CtMethod, InitializationGatheringEditor> gatherMap = createInitializationGatheringMap(contractInfo,
				currentContractClass, storeIndex);
		for (CtMethod contractMethod : currentContractClass.getDeclaredMethods()) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("transforming behavior " + contractMethod.getLongName());
			}
			transform(contractInfo, contractMethod, storeIndex, new ContractMethodDependencies(gatherMap,
					contractMethod));
		}
	}

	private Map<CtMethod, InitializationGatheringEditor> createInitializationGatheringMap(ContractInfo contractInfo,
			CtClass currentContractClass, AtomicInteger storeIndex) throws CannotCompileException {
		Map<CtMethod, InitializationGatheringEditor> gatherMap = new HashMap<CtMethod, InitializationGatheringEditor>();
		for (CtMethod contractMethod : currentContractClass.getDeclaredMethods()) {
			InitializationGatheringEditor gatheringEditor = new InitializationGatheringEditor(storeIndex, contractInfo);
			contractMethod.instrument(gatheringEditor);
			gatherMap.put(contractMethod, gatheringEditor);
		}
		return gatherMap;
	}

	public void transform(ContractInfo contractInfo, CtMethod contractMethod, AtomicInteger storeIndex,
			ContractMethodDependencies contractMethodDependencies) throws Exception {
		ContractMethodConditionEditor expressionEditor = new ContractMethodConditionEditor(contractInfo);
		contractMethod.instrument(expressionEditor);
		contractInfo.addMethod(contractMethod, expressionEditor.hasPreCondition()
				|| contractMethodDependencies.hasPreDependencies(), expressionEditor.isPostConditionAvailable(),
				contractMethodDependencies.containsUnchanged());
		if (contractMethodDependencies.getThrownException() != null) {
			contractInfo.addError(contractMethodDependencies.getThrownException());
		}
		if (contractMethodDependencies.hasPreDependencies()) {
			insertStoreDependencies(contractMethod, contractMethodDependencies);
		}
	}

	private void insertStoreDependencies(CtMethod contractMethod, ContractMethodDependencies contractMethodDependencies)
			throws BadBytecode, CannotCompileException, NotFoundException {
		if (isClassInvariant(contractMethod)) {
			insertStoreDependenciesForClassInvariant(contractMethod, contractMethodDependencies);
		} else {
			insertStoreDependenciesForPostCondition(contractMethod, contractMethodDependencies);
		}
	}

	private void insertStoreDependenciesForClassInvariant(CtMethod contractMethod,
			ContractMethodDependencies contractMethodDependencies) throws BadBytecode, CannotCompileException,
			NotFoundException {
		CtMethod beforeInvariant = CtNewMethod.make(CtClass.voidType,
				makeBeforeInvariantHelperMethodName(contractMethod), new CtClass[0],
				contractMethod.getExceptionTypes(), null, contractMethod.getDeclaringClass());
		contractMethod.getDeclaringClass().addMethod(beforeInvariant);
		addBehaviorAnnotation(beforeInvariant, ClassFilePool.INSTANCE.getClass(BeforeClassInvariant.class));
		insertIntoBeforeInvariant(contractMethodDependencies, beforeInvariant, contractMethod.getDeclaringClass());
	}

	private void insertIntoBeforeInvariant(ContractMethodDependencies contractMethodDependencies,
			CtMethod beforeInvariant, CtClass contractClass) throws CannotCompileException, BadBytecode {
		if (!contractMethodDependencies.getPreConditionExp().isEmpty()) {
			contractMethodDependencies.getPreConditionExp().insertBefore(beforeInvariant);
		}
		if (contractMethodDependencies.hasStoreDependencies()) {
			ConstPool constPool = beforeInvariant.getMethodInfo().getConstPool();
			CodeAttribute attribute = beforeInvariant.getMethodInfo().getCodeAttribute();
			new OldStoreCallWriter(constPool, contractClass).insertOldStoreCalls(attribute, contractMethodDependencies
					.getStoreDependencies());
			attribute.computeMaxStack();
		}
	}

	private void insertStoreDependenciesForPostCondition(CtMethod contractMethod,
			ContractMethodDependencies contractMethodDependencies) throws BadBytecode, CannotCompileException {
		if (!contractMethodDependencies.getPreConditionExp().isEmpty()) {
			IfExp isBeforeCondition = new IfExp(new StaticCallExp(Evaluator.isBefore));
			isBeforeCondition.addIfBody(contractMethodDependencies.getPreConditionExp());
			isBeforeCondition.insertBefore(contractMethod);
		}
		if (contractMethodDependencies.hasStoreDependencies()) {
			ConstPool constPool = contractMethod.getMethodInfo().getConstPool();
			CodeAttribute attribute = contractMethod.getMethodInfo().getCodeAttribute();
			int ifBlockLength = new OldStoreCallWriter(constPool, contractMethod.getDeclaringClass())
					.insertOldStoreCalls(attribute, contractMethodDependencies.getStoreDependencies());
			insertJump(attribute.iterator(), ifBlockLength, constPool);
		}
	}

	private void insertJump(CodeIterator iterator, int ifBlockLength, ConstPool constPool) throws BadBytecode {
		int jumpLength = ifBlockLength + 3;
		byte[] ifBytes = new byte[6];
		ifBytes[0] = (byte) Opcode.INVOKESTATIC;
		setMethodIndex(constPool, ifBytes, 1, Evaluator.isBefore, Evaluator.isBeforeDescriptor);
		ifBytes[3] = (byte) Opcode.IFEQ;
		ifBytes[4] = (byte) (jumpLength >> 8);
		ifBytes[5] = (byte) jumpLength;
		iterator.insertEx(0, ifBytes);
	}

}
