package de.vksi.c4j.internal.util;

import static javassist.bytecode.Opcode.ALOAD;
import static javassist.bytecode.Opcode.ALOAD_1;
import static javassist.bytecode.Opcode.ALOAD_2;
import static javassist.bytecode.Opcode.ALOAD_3;
import static javassist.bytecode.Opcode.DLOAD;
import static javassist.bytecode.Opcode.DLOAD_1;
import static javassist.bytecode.Opcode.DLOAD_2;
import static javassist.bytecode.Opcode.DLOAD_3;
import static javassist.bytecode.Opcode.FLOAD;
import static javassist.bytecode.Opcode.FLOAD_1;
import static javassist.bytecode.Opcode.FLOAD_2;
import static javassist.bytecode.Opcode.FLOAD_3;
import static javassist.bytecode.Opcode.GETFIELD;
import static javassist.bytecode.Opcode.GETSTATIC;
import static javassist.bytecode.Opcode.ILOAD;
import static javassist.bytecode.Opcode.ILOAD_1;
import static javassist.bytecode.Opcode.ILOAD_2;
import static javassist.bytecode.Opcode.ILOAD_3;
import static javassist.bytecode.Opcode.INVOKEINTERFACE;
import static javassist.bytecode.Opcode.INVOKESPECIAL;
import static javassist.bytecode.Opcode.INVOKESTATIC;
import static javassist.bytecode.Opcode.INVOKEVIRTUAL;
import static javassist.bytecode.Opcode.LLOAD;
import static javassist.bytecode.Opcode.LLOAD_1;
import static javassist.bytecode.Opcode.LLOAD_2;
import static javassist.bytecode.Opcode.LLOAD_3;
import static javassist.bytecode.Opcode.MULTIANEWARRAY;
import static javassist.bytecode.Opcode.PUTFIELD;
import static javassist.bytecode.Opcode.PUTSTATIC;
import static javassist.bytecode.Opcode.WIDE;

import java.util.Arrays;
import java.util.LinkedList;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.RootTransformer;

public class Stackalyzer {

	private static final String METHODREF_CONSTRUCTOR = "<init>";

	private static class StackDepth extends Pair<Integer, Integer> {
		public StackDepth(int index, int depth) {
			super(index, depth);
		}
	}

	public byte[] getDependenciesFor(CtBehavior contractBehavior, int indexOfDependentCall) throws BadBytecode,
			NotFoundException, UsageError {
		CodeAttribute ca = contractBehavior.getMethodInfo().getCodeAttribute();
		CodeIterator ci = ca.iterator();
		LinkedList<StackDepth> stackDepth = new LinkedList<StackDepth>();
		int depth = 0;
		while (ci.hasNext()) {
			int index = ci.next();
			if (index > indexOfDependentCall) {
				break;
			}
			int op = ci.byteAt(index);
			stackDepth.add(new StackDepth(index, depth));
			depth += getOpcodeDelta(op, index, ci, contractBehavior.getMethodInfo().getConstPool());
		}
		int stackDepthForOldCall = depth;
		while (stackDepth.getLast().getSecond().intValue() >= stackDepthForOldCall) {
			stackDepth.removeLast();
		}
		checkLocalVarAccess(ci, stackDepth.getLast().getFirst().intValue(), indexOfDependentCall, contractBehavior
				.getParameterTypes().length);
		return Arrays.copyOfRange(ca.getCode(), stackDepth.getLast().getFirst().intValue(), indexOfDependentCall);
	}

	private void checkLocalVarAccess(CodeIterator ci, int beginIndex, int endIndex, int numParams) throws BadBytecode,
			UsageError {
		ci.move(beginIndex);
		while (ci.hasNext()) {
			int index = ci.next();
			if (index > endIndex) {
				return;
			}
			int op = ci.byteAt(index);
			checkLoadOpcodes(ci, numParams, index, op, false);
		}
	}

	private void checkLoadOpcodes(CodeIterator ci, int numParams, int index, int op, boolean wide) throws UsageError {
		switch (op) {
			case ALOAD_1:
			case DLOAD_1:
			case FLOAD_1:
			case ILOAD_1:
			case LLOAD_1:
				verifyLocalVarAccess(1, numParams);
				break;
			case ALOAD_2:
			case DLOAD_2:
			case FLOAD_2:
			case ILOAD_2:
			case LLOAD_2:
				verifyLocalVarAccess(2, numParams);
				break;
			case ALOAD_3:
			case DLOAD_3:
			case FLOAD_3:
			case ILOAD_3:
			case LLOAD_3:
				verifyLocalVarAccess(3, numParams);
				break;
			case ALOAD:
			case DLOAD:
			case FLOAD:
			case ILOAD:
			case LLOAD:
				int register = wide ? ci.u16bitAt(index + 1) : ci.byteAt(index + 1);
				verifyLocalVarAccess(register, numParams);
				break;
			case WIDE:
				int loadOp = ci.byteAt(index + 1);
				checkLoadOpcodes(ci, numParams, index + 1, loadOp, true);
				break;
		}
	}

	private void verifyLocalVarAccess(int i, int numParams) throws UsageError {
		if (i > numParams) {
			throw new UsageError("Illegal access on local variable within old().");
		}
	}

	private int getOpcodeDelta(int op, int index, CodeIterator ci, ConstPool constPool) throws BadBytecode,
			NotFoundException, UsageError {
		switch (op) {
			case GETFIELD:
			case GETSTATIC:
				// either +0 or +1
				return getStaticFieldDelta(index, ci, constPool) - 1;
			case PUTFIELD:
				// either -2 or -3
				return -1 * getStaticFieldDelta(index, ci, constPool) - 1;
			case PUTSTATIC:
				// either -1 or -2
				return -1 * getStaticFieldDelta(index, ci, constPool);
			case INVOKEINTERFACE:
				return getStaticBehaviorDelta(getBehaviorFromInterfaceMethodrefInfo(index, ci, constPool)) - 1;
			case INVOKESPECIAL:
			case INVOKEVIRTUAL:
				// - (# method params (* 2 if double/long)) - 1 + 0-2
				return getStaticBehaviorDelta(getBehaviorFromMethodrefInfo(index, ci, constPool)) - 1;
			case INVOKESTATIC:
				// - (# method params (* 2 if double/long)) + 0-2
				return getStaticBehaviorDelta(getBehaviorFromMethodrefInfo(index, ci, constPool));
			case MULTIANEWARRAY:
				return ci.byteAt(index + 3) - 1;
			case WIDE:
				return getOpcodeDelta(ci.byteAt(index + 1), index, ci, constPool);
			default:
				return Opcode.STACK_GROW[op];
		}
	}

	private int getStaticFieldDelta(int index, CodeIterator ci, ConstPool constPool) throws NotFoundException {
		int fieldIndex = ci.u16bitAt(index + 1);
		CtClass fieldClass = RootTransformer.INSTANCE.getPool().get(constPool.getFieldrefClassName(fieldIndex));
		return getTypeDelta(fieldClass.getField(constPool.getFieldrefName(fieldIndex)).getType());
	}

	private CtBehavior getBehaviorFromInterfaceMethodrefInfo(int index, CodeIterator ci, ConstPool constPool)
			throws NotFoundException {
		int methodIndex = ci.u16bitAt(index + 1);
		CtClass behaviorClass = RootTransformer.INSTANCE.getPool().get(
				constPool.getInterfaceMethodrefClassName(methodIndex));
		String behaviorName = constPool.getInterfaceMethodrefName(methodIndex);
		String behaviorDescriptor = constPool.getInterfaceMethodrefType(methodIndex);
		return getBehaviorFromInfo(behaviorClass, behaviorName, behaviorDescriptor);
	}

	private CtBehavior getBehaviorFromMethodrefInfo(int index, CodeIterator ci, ConstPool constPool)
			throws NotFoundException {
		int methodIndex = ci.u16bitAt(index + 1);
		CtClass behaviorClass = RootTransformer.INSTANCE.getPool().get(constPool.getMethodrefClassName(methodIndex));
		String behaviorName = constPool.getMethodrefName(methodIndex);
		String behaviorDescriptor = constPool.getMethodrefType(methodIndex);
		return getBehaviorFromInfo(behaviorClass, behaviorName, behaviorDescriptor);
	}

	private CtBehavior getBehaviorFromInfo(CtClass behaviorClass, String behaviorName, String behaviorDescriptor)
			throws NotFoundException {
		if (behaviorName.equals(METHODREF_CONSTRUCTOR)) {
			return behaviorClass.getConstructor(behaviorDescriptor);
		}
		return behaviorClass.getMethod(behaviorName, behaviorDescriptor);
	}

	private int getStaticBehaviorDelta(CtBehavior behavior) throws NotFoundException {
		int delta = 0;
		for (CtClass paramType : behavior.getParameterTypes()) {
			delta -= getTypeDelta(paramType);
		}
		if (behavior instanceof CtMethod) {
			delta += getTypeDelta(((CtMethod) behavior).getReturnType());
		}
		return delta;
	}

	private int getTypeDelta(CtClass type) {
		if (type.equals(CtClass.doubleType) || type.equals(CtClass.longType)) {
			return 2;
		}
		if (type.equals(CtClass.voidType)) {
			return 0;
		}
		return 1;
	}
}
