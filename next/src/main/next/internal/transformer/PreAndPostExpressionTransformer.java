package next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import next.Condition;
import next.internal.Evaluator;
import next.internal.TransformationException;

public class PreAndPostExpressionTransformer extends ContractDeclaredMethodTransformer {

	@Override
	public void transform(CtMethod contractMethod, CtClass targetClass) throws Exception {
		MethodInfo minfo = contractMethod.getMethodInfo();
		ConstPool constPool = minfo.getConstPool();
		CodeAttribute ca = minfo.getCodeAttribute();
		CodeIterator ci = ca.iterator();
		int preIndex = -1;
		int postIndex = -1;
		int returnIndex = -1;
		while (ci.hasNext()) {
			int index = ci.next();
			int op = ci.byteAt(index);
			if (op == Opcode.RETURN) {
				if (returnIndex >= 0) {
					throw new TransformationException(
							"there can only be a single return-statement within a contract-method.");
				}
				returnIndex = index;
			} else if (op == Opcode.INVOKESTATIC) {
				int constPoolIndex = ci.s16bitAt(index + 1);
				String className = constPool.getMethodrefClassName(constPoolIndex);
				String methodName = constPool.getMethodrefName(constPoolIndex);
				if (className.equals(Condition.class.getName())) {
					if (methodName.equals("pre")) {
						if (preIndex >= 0) {
							throw new TransformationException("pre() can only be used once within a contract-method.");
						}
						if (postIndex >= 0) {
							throw new TransformationException("pre() must be called before post().");
						}
						preIndex = index;
					} else if (methodName.equals("post")) {
						if (postIndex >= 0) {
							throw new TransformationException("post() can only be used once within a contract-method.");
						}
						postIndex = index;
					} else if (methodName.equals("ignored")) {
						if (returnIndex >= 0) {
							throw new TransformationException(
									"there can only be a single return-statement within a contract-method.");
						}
						returnIndex = index;
					}
				}
			}
		}
		if (returnIndex == -1) {
			throw new TransformationException(
					"'return ignored();' must be used for contract-methods with return-values.");
		}
		Integer classRef = null;
		if (preIndex >= 0) {
			classRef = Integer.valueOf(constPool.addClassInfo(Evaluator.class.getName()));
			int methodRef = constPool.addMethodrefInfo(classRef.intValue(), Evaluator.isBefore.getCallMethod(),
					Descriptor.ofMethod(CtClass.booleanType, new CtClass[0]));
			int value;
			if (postIndex >= 0) {
				value = postIndex - preIndex;
			} else {
				value = returnIndex - preIndex;
			}
			value += 0; // +2 for IFEQ params, -2 for INVOKESTATIC params
			byte[] insert = new byte[] { (byte) Opcode.INVOKESTATIC, (byte) (methodRef >>> 8), (byte) methodRef,
					(byte) Opcode.IFEQ, (byte) (value >>> 8), (byte) value };
			ci.insert(preIndex + 3, insert);
		}
		if (postIndex >= 0) {
			if (classRef == null) {
				classRef = Integer.valueOf(constPool.addClassInfo(Evaluator.class.getName()));
			}
			int methodRef = constPool.addMethodrefInfo(classRef.intValue(), Evaluator.isAfter.getCallMethod(),
					Descriptor.ofMethod(CtClass.booleanType, new CtClass[0]));
			int value = returnIndex - postIndex;
			byte[] insert = new byte[] { (byte) Opcode.INVOKESTATIC, (byte) (methodRef >>> 8), (byte) methodRef,
					(byte) Opcode.IFEQ, (byte) (value >>> 8), (byte) value };
			ci.insert(postIndex + 3, insert);
		}
	}

}
