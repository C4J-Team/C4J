package de.vksi.c4j.internal.transformer.util;

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
import static javassist.bytecode.Opcode.ILOAD;
import static javassist.bytecode.Opcode.ILOAD_1;
import static javassist.bytecode.Opcode.ILOAD_2;
import static javassist.bytecode.Opcode.ILOAD_3;
import static javassist.bytecode.Opcode.LLOAD;
import static javassist.bytecode.Opcode.LLOAD_1;
import static javassist.bytecode.Opcode.LLOAD_2;
import static javassist.bytecode.Opcode.LLOAD_3;
import static javassist.bytecode.Opcode.WIDE;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import de.vksi.c4j.error.UsageError;

public class VariableAccessVerifier {
	private static Verifier[] opMap = new Verifier[256];
	private final CodeIterator codeIterator;
	private final int numParams;

	static {
		opMap[ALOAD_1] = opMap[DLOAD_1] = opMap[FLOAD_1] = opMap[ILOAD_1] = opMap[LLOAD_1] = new LoadCommonVerifier(1);
		opMap[ALOAD_2] = opMap[DLOAD_2] = opMap[FLOAD_2] = opMap[ILOAD_2] = opMap[LLOAD_2] = new LoadCommonVerifier(2);
		opMap[ALOAD_3] = opMap[DLOAD_3] = opMap[FLOAD_3] = opMap[ILOAD_3] = opMap[LLOAD_3] = new LoadCommonVerifier(3);
		opMap[ALOAD] = opMap[DLOAD] = opMap[FLOAD] = opMap[ILOAD] = opMap[LLOAD] = new LoadGenericVerifier();
		opMap[WIDE] = new WideVerifier();
	}

	public VariableAccessVerifier(CodeIterator codeIterator, int numParams) {
		this.codeIterator = codeIterator;
		this.numParams = numParams;
	}

	public int getNumParams() {
		return numParams;
	}

	public CodeIterator getCodeIterator() {
		return codeIterator;
	}

	public boolean verify(int beginIndex, int endIndex) throws BadBytecode, UsageError {
		codeIterator.move(beginIndex);
		while (codeIterator.hasNext()) {
			int index = codeIterator.next();
			if (index > endIndex) {
				return false;
			}
			if (verifyOp(index, false)) {
				return true;
			}
		}
		return false;
	}

	private boolean verifyOp(int index, boolean wide) throws UsageError {
		int op = codeIterator.byteAt(index);
		if (opMap[op] != null) {
			return opMap[op].verify(this, index, wide);
		}
		return false;
	}

	private static boolean verifyLocalVarAccess(int register, int numParams) throws UsageError {
		return register > numParams;
	}

	interface Verifier {
		boolean verify(VariableAccessVerifier verifier, int index, boolean wide);
	}

	private static class LoadCommonVerifier implements Verifier {
		private final int register;

		public LoadCommonVerifier(int register) {
			this.register = register;
		}

		@Override
		public boolean verify(VariableAccessVerifier verifier, int index, boolean wide) {
			return verifyLocalVarAccess(register, verifier.getNumParams());
		}
	}

	private static class LoadGenericVerifier implements Verifier {
		@Override
		public boolean verify(VariableAccessVerifier verifier, int index, boolean wide) {
			int register = wide ? verifier.getCodeIterator().u16bitAt(index + 1) : verifier.getCodeIterator().byteAt(
					index + 1);
			return verifyLocalVarAccess(register, verifier.getNumParams());
		}
	}

	private static class WideVerifier implements Verifier {

		@Override
		public boolean verify(VariableAccessVerifier verifier, int index, boolean wide) {
			return verifier.verifyOp(index + 1, true);
		}

	}
}
