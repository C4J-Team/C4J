package de.andrena.next.internal.util;

import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

public class ByteCodePrinter {
	public ByteCodePrinter(CtMethod contractMethod, CodeAttribute ca) throws BadBytecode {
		CodeIterator ci = ca.iterator();
		while (ci.hasNext()) {
			int index = ci.next();
			int op = ci.byteAt(index);
			System.out.println(index + ": " + op + ", " + Mnemonic.OPCODE[op]);
			if (op == Opcode.INVOKESTATIC || op == Opcode.IFEQ || op == Opcode.IFNE || op == Opcode.GETSTATIC) {
				int constPoolIndex = ci.s16bitAt(index + 1);
				System.out.println("param: " + constPoolIndex);
				if (op == Opcode.INVOKESTATIC) {
					String className = contractMethod.getMethodInfo().getConstPool()
							.getMethodrefClassName(constPoolIndex);
					String methodName = contractMethod.getMethodInfo().getConstPool().getMethodrefName(constPoolIndex);
					System.out.println("class: " + className);
					System.out.println("method: " + methodName);
				}
				if (op == Opcode.GETSTATIC) {
					System.out.println("class: "
							+ contractMethod.getMethodInfo().getConstPool().getFieldrefClassName(constPoolIndex));
					System.out.println("field: "
							+ contractMethod.getMethodInfo().getConstPool().getFieldrefName(constPoolIndex));
				}
			}
		}
	}
}
