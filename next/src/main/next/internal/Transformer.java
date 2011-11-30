package next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import next.Contract;
import next.internal.compiler.ArrayExp;
import next.internal.compiler.NestedExp;
import next.internal.compiler.StaticCallExp;
import next.internal.compiler.ValueExp;
import next.internal.util.BackdoorAnnotationLoader;
import next.internal.util.ObjectConverter;

import org.apache.log4j.Logger;

public class Transformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(Transformer.class);
	private ClassPool pool = ClassPool.getDefault();

	public static final String PRE_PREFIX = "pre$";
	public static final String POST_PREFIX = "post$";

	@Override
	public byte[] transform(ClassLoader loader, String classNameWithSlashes, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		String className = classNameWithSlashes.replace('/', '.');
		logger.debug("transformation started for class " + className);
		try {
			if (loader != null) {
				pool.insertClassPath(new LoaderClassPath(loader));
			}
			if (classfileBuffer != null) {
				pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
			}

			CtClass targetClass = pool.get(className);
			if (targetClass.isInterface()) {
				logger.debug("transformation aborted, as class is an interface");
				return null;
			}
			if (targetClass.hasAnnotation(Contract.class)) {
				logger.info("transforming class " + className);
				String contractClassString = new BackdoorAnnotationLoader(targetClass).getClassValue(Contract.class,
						"value");
				transformClass(targetClass, pool.get(contractClassString));
				return targetClass.toBytecode();
			} else if (targetClass.getSuperclass() != null && targetClass.getSuperclass().hasAnnotation(Contract.class)) {
				logger.info("transforming contract " + className);
				transformContractClass(targetClass);
				return targetClass.toBytecode();
			}
		} catch (NotFoundException e) {
			logger.warn("transformation failed for class '" + className + "': could not load class", e);
		} catch (TransformationException e) {
			logger.warn(e.getMessage(), e);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.warn("transformation failed for class '" + className + "': unknown error", e);
		}
		return null;
	}

	private void transformContractClass(CtClass contractClass) throws CannotCompileException, BadBytecode,
			TransformationException, NotFoundException {
		for (CtMethod contractMethod : contractClass.getDeclaredMethods()) {
			MethodInfo minfo = contractMethod.getMethodInfo();
			ConstPool constPool = minfo.getConstPool();
			CodeAttribute ca = minfo.getCodeAttribute();
			CodeIterator ci = ca.iterator();
			// printCurrentByteCode(contractMethod, ca);
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
					if (className.equals("next.Condition")) {
						if (methodName.equals("pre")) {
							if (preIndex >= 0) {
								throw new TransformationException(
										"pre() can only be used once within a contract-method.");
							}
							if (postIndex >= 0) {
								throw new TransformationException("pre() must be called before post().");
							}
							preIndex = index;
						} else if (methodName.equals("post")) {
							if (postIndex >= 0) {
								throw new TransformationException(
										"post() can only be used once within a contract-method.");
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
			// printCurrentByteCode(contractMethod, ca);
			contractMethod.instrument(new ContractMethodExpressionEditor(contractClass.getSuperclass()));
		}

	}

	private void printCurrentByteCode(CtMethod contractMethod, CodeAttribute ca) throws BadBytecode {
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

	private void transformClass(CtClass targetClass, CtClass contractClass) throws TransformationException,
			CannotCompileException, NotFoundException {
		for (CtMethod contractMethod : contractClass.getDeclaredMethods()) {
			for (CtMethod targetMethod : targetClass.getDeclaredMethods()) {
				if (contractMethod.equals(targetMethod)) {
					ArrayExp paramTypesArray = ArrayExp.forParamTypes(targetMethod);
					ArrayExp argsArray = ArrayExp.forArgs(targetMethod);
					StaticCallExp callBefore = new StaticCallExp(Evaluator.before, NestedExp.THIS, new ValueExp(
							contractClass), new ValueExp(contractMethod.getName()), paramTypesArray, argsArray);
					NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
					if (contractMethod.getReturnType().equals(CtClass.voidType)) {
						returnValue = NestedExp.NULL;
					}
					StaticCallExp callAfter = new StaticCallExp(Evaluator.after, NestedExp.THIS, new ValueExp(
							contractClass), new ValueExp(contractMethod.getName()), paramTypesArray, argsArray,
							returnValue);
					logger.info("callstring: " + callBefore);
					callBefore.toStandalone().insertBefore(targetMethod);
					callAfter.toStandalone().insertAfter(targetMethod);
				}
			}
		}
	}
}
