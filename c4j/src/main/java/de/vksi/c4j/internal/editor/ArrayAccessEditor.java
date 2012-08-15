package de.vksi.c4j.internal.editor;

import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import de.vksi.c4j.internal.evaluator.PureEvaluator;
import de.vksi.c4j.internal.util.TransformationHelper;

public class ArrayAccessEditor {
	private TransformationHelper transformationHelper = new TransformationHelper();
	private static final Set<Integer> ARRAY_STORE_OPCODES = new HashSet<Integer>();

	static {
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.AASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.BASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.CASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.DASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.FASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.IASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.LASTORE);
		addToOpcodes(ARRAY_STORE_OPCODES, Opcode.SASTORE);
	}

	private static void addToOpcodes(Set<Integer> arrayStoreOpcodes, int opcode) {
		arrayStoreOpcodes.add(Integer.valueOf(opcode));
	}

	public void instrumentArrayAccesses(CtBehavior affectedMethod) throws CannotCompileException {
		try {
			CodeAttribute ca = affectedMethod.getMethodInfo().getCodeAttribute();
			CodeIterator ci = ca.iterator();
			byte[] checkUnpureBytes = getCheckUnpureBytes(affectedMethod);
			byte[] checkUnpureBytesLong = getCheckUnpureBytesLong(affectedMethod);
			while (ci.hasNext()) {
				checkAndReplaceArrayAccess(ci, checkUnpureBytes, checkUnpureBytesLong);
			}
			ca.computeMaxStack();
		} catch (BadBytecode e) {
			throw new CannotCompileException(e);
		}
	}

	private int checkAndReplaceArrayAccess(CodeIterator ci, byte[] checkUnpureBytes, byte[] checkUnpureBytesLong)
			throws BadBytecode {
		int index = ci.next();
		int op = ci.byteAt(index);
		if (ARRAY_STORE_OPCODES.contains(Integer.valueOf(op))) {
			handleArrayStoreOpcodes(ci, checkUnpureBytes, checkUnpureBytesLong, index, op);
		}
		return op;
	}

	private void handleArrayStoreOpcodes(CodeIterator ci, byte[] checkUnpureBytes, byte[] checkUnpureBytesLong,
			int index, int op) throws BadBytecode {
		if (isDoubleOrLongArrayStoreOpcode(op)) {
			ci.insert(index, checkUnpureBytesLong);
			return;
		}
		ci.insert(index, checkUnpureBytes);
	}

	private boolean isDoubleOrLongArrayStoreOpcode(int op) {
		return op == Opcode.DASTORE || op == Opcode.LASTORE;
	}

	private byte[] getCheckUnpureBytesLong(CtBehavior affectedMethod) {
		byte[] checkUnpureBytesLong = new byte[7];
		checkUnpureBytesLong[0] = Opcode.DUP2_X2;
		checkUnpureBytesLong[1] = Opcode.POP2;
		checkUnpureBytesLong[2] = Opcode.DUP2_X2;
		checkUnpureBytesLong[3] = Opcode.POP;
		checkUnpureBytesLong[4] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(affectedMethod.getMethodInfo().getConstPool(), checkUnpureBytesLong, 5,
				PureEvaluator.checkUnpureAccess, "(Ljava/lang/Object;)V");
		return checkUnpureBytesLong;
	}

	private byte[] getCheckUnpureBytes(CtBehavior affectedMethod) {
		byte[] checkUnpureBytes = new byte[6];
		checkUnpureBytes[0] = Opcode.DUP2_X1;
		checkUnpureBytes[1] = Opcode.POP2;
		checkUnpureBytes[2] = Opcode.DUP_X2;
		checkUnpureBytes[3] = (byte) Opcode.INVOKESTATIC;
		transformationHelper.setMethodIndex(affectedMethod.getMethodInfo().getConstPool(), checkUnpureBytes, 4,
				PureEvaluator.checkUnpureAccess, "(Ljava/lang/Object;)V");
		return checkUnpureBytes;
	}
}
