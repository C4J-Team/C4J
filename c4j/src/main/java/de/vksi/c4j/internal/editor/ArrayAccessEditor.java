package de.vksi.c4j.internal.editor;

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
		if (op == Opcode.AASTORE || op == Opcode.BASTORE || op == Opcode.CASTORE || op == Opcode.DASTORE
				|| op == Opcode.FASTORE || op == Opcode.IASTORE || op == Opcode.LASTORE || op == Opcode.SASTORE) {
			if (op == Opcode.DASTORE || op == Opcode.LASTORE) {
				ci.insert(index, checkUnpureBytesLong);
			} else {
				ci.insert(index, checkUnpureBytes);
			}
		}
		return op;
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
