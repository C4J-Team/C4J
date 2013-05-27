package de.vksi.c4j.internal.transformer.util;

import static javassist.bytecode.Opcode.GETFIELD;
import static javassist.bytecode.Opcode.GETSTATIC;
import static javassist.bytecode.Opcode.INVOKEINTERFACE;
import static javassist.bytecode.Opcode.INVOKESPECIAL;
import static javassist.bytecode.Opcode.INVOKESTATIC;
import static javassist.bytecode.Opcode.INVOKEVIRTUAL;
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
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.types.Pair;

public class Stackalyzer {

	private static final String METHODREF_CONSTRUCTOR = "<init>";

	private static class StackDepth extends Pair<Integer, Integer> {
		public StackDepth(int index, int depth) {
			super(index, depth);
		}
	}

	public byte[] getDependenciesFor(CtBehavior contractBehavior, int indexOfDependentCall) throws BadBytecode,
			NotFoundException, UsageError {
		CodeAttribute codeAttribute = contractBehavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();
		int indexOfNestedExpression = getIndexOfNestedExpression(contractBehavior,
				indexOfDependentCall, codeIterator);
		VariableAccessVerifier verifier = new VariableAccessVerifier(codeIterator,
				contractBehavior.getParameterTypes().length);
		if (verifier.verify(indexOfNestedExpression, indexOfDependentCall)) {
			throw new UsageError("Illegal access on local variable within old().");
		}
		return Arrays.copyOfRange(codeAttribute.getCode(), indexOfNestedExpression, indexOfDependentCall);
	}

	private int getIndexOfNestedExpression(CtBehavior contractBehavior, int indexOfDependentCall,
			CodeIterator codeIterator) throws BadBytecode, NotFoundException, UsageError {
		LinkedList<StackDepth> stackDepth = new LinkedList<StackDepth>();
		int depth = 0;
		while (codeIterator.hasNext()) {
			int index = codeIterator.next();
			if (index > indexOfDependentCall) {
				break;
			}
			int op = codeIterator.byteAt(index);
			stackDepth.add(new StackDepth(index, depth));
			depth += getOpcodeDelta(op, index, codeIterator, contractBehavior.getMethodInfo().getConstPool());
		}
		int stackDepthForOldCall = depth;
		while (stackDepth.getLast().getSecond().intValue() >= stackDepthForOldCall) {
			stackDepth.removeLast();
		}
		return stackDepth.getLast().getFirst().intValue();
	}

	private int getOpcodeDelta(int op, int index, CodeIterator ci, ConstPool constPool) throws BadBytecode,
			NotFoundException, UsageError {
		switch (op) {
			case GETFIELD:
				// either +0 or +1
				return getStaticFieldDelta(index, ci, constPool) - 1;
			case GETSTATIC:
				// either +1 or +2
				return getStaticFieldDelta(index, ci, constPool);
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
		CtClass fieldClass = ClassFilePool.INSTANCE.getClass(constPool.getFieldrefClassName(fieldIndex));
		return getTypeDelta(fieldClass.getField(constPool.getFieldrefName(fieldIndex)).getType());
	}

	private CtBehavior getBehaviorFromInterfaceMethodrefInfo(int index, CodeIterator ci, ConstPool constPool)
			throws NotFoundException {
		int methodIndex = ci.u16bitAt(index + 1);
		CtClass behaviorClass = ClassFilePool.INSTANCE.getClass(constPool.getInterfaceMethodrefClassName(methodIndex));
		String behaviorName = constPool.getInterfaceMethodrefName(methodIndex);
		String behaviorDescriptor = constPool.getInterfaceMethodrefType(methodIndex);
		return getBehaviorFromInfo(behaviorClass, behaviorName, behaviorDescriptor);
	}

	private CtBehavior getBehaviorFromMethodrefInfo(int index, CodeIterator ci, ConstPool constPool)
			throws NotFoundException {
		int methodIndex = ci.u16bitAt(index + 1);
		CtClass behaviorClass = ClassFilePool.INSTANCE.getClass(constPool.getMethodrefClassName(methodIndex));
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
