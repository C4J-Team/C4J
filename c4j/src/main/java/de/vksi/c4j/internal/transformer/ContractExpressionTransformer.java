package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.TransformationHelper.addBehaviorAnnotation;
import static de.vksi.c4j.internal.util.TransformationHelper.setClassIndex;
import static de.vksi.c4j.internal.util.TransformationHelper.setMethodIndex;

import java.util.HashMap;
import java.util.List;
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
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.editor.ContractMethodConditionEditor;
import de.vksi.c4j.internal.editor.InitializationGatheringEditor;
import de.vksi.c4j.internal.editor.StoreDependency;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.OldCache;
import de.vksi.c4j.internal.evaluator.PureEvaluator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractExpressionTransformer extends AbstractContractClassTransformer {

	public static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before";
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		AtomicInteger storeIndex = new AtomicInteger();
		Map<CtMethod, InitializationGatheringEditor> gatherMap = createInitializationGatheringMap(contractInfo,
				currentContractClass, storeIndex);
		for (CtMethod contractMethod : currentContractClass.getDeclaredMethods()) {
			if (logger.isTraceEnabled()) {
				logger.trace("transforming behavior " + contractMethod.getLongName());
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
		ContractMethodConditionEditor expressionEditor = new ContractMethodConditionEditor(rootTransformer,
				contractInfo);
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
		if (contractMethod.hasAnnotation(ClassInvariant.class)) {
			insertStoreDependenciesForClassInvariant(contractMethod, contractMethodDependencies);
		} else {
			insertStoreDependenciesForPostCondition(contractMethod, contractMethodDependencies);
		}
	}

	private void insertStoreDependenciesForClassInvariant(CtMethod contractMethod,
			ContractMethodDependencies contractMethodDependencies) throws BadBytecode, CannotCompileException,
			NotFoundException {
		CtMethod beforeInvariant = CtNewMethod.make(CtClass.voidType, contractMethod.getName()
				+ BEFORE_INVARIANT_METHOD_SUFFIX, new CtClass[0], contractMethod.getExceptionTypes(), null,
				contractMethod.getDeclaringClass());
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
			insertOldStoreCalls(attribute, contractMethodDependencies.getStoreDependencies(), constPool, contractClass);
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
			int ifBlockLength = insertOldStoreCalls(attribute, contractMethodDependencies.getStoreDependencies(),
					constPool, contractMethod.getDeclaringClass());
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
		setMethodIndex(constPool, registerUnchangeableBytes, 2, PureEvaluator.registerUnchangeable,
				PureEvaluator.registerUnchangeableDescriptor);
		return registerUnchangeableBytes;
	}

	private byte[] getContractClassBytes(ConstPool constPool, CtClass contractClass) {
		byte[] contractClassBytes = new byte[3];
		contractClassBytes[0] = (byte) Opcode.LDC_W;
		setClassIndex(constPool, contractClassBytes, 1, contractClass);
		return contractClassBytes;
	}

	private byte[] getOldStoreBytes(ConstPool constPool, StaticCall oldStoreCall) {
		byte[] oldStoreBytes = new byte[3];
		oldStoreBytes[0] = (byte) Opcode.INVOKESTATIC;
		setMethodIndex(constPool, oldStoreBytes, 1, oldStoreCall, OldCache.oldStoreDescriptor);
		return oldStoreBytes;
	}

}
