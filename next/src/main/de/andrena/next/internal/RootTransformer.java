package de.andrena.next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

import org.apache.log4j.Logger;

import de.andrena.next.Contract;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.transformer.TargetClassTransformer;
import de.andrena.next.internal.util.BackdoorAnnotationLoader;

public class RootTransformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(getClass());
	private ClassPool pool = ClassPool.getDefault();

	private TargetClassTransformer targetClassTransformer = new TargetClassTransformer();
	private ContractClassTransformer contractClassTransformer = new ContractClassTransformer(pool);

	public static Set<String> contractClasses = new HashSet<String>();

	private static Throwable lastException;

	public static Throwable getLastException() {
		return lastException;
	}

	@Override
	public byte[] transform(ClassLoader loader, String classNameWithSlashes, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		String className = classNameWithSlashes.replace('/', '.');
		logger.debug("transformation started for class " + className);
		try {
			updateClassPath(loader, classfileBuffer, className);
			return transformClass(className);
		} catch (TransformationException e) {
			lastException = e;
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			lastException = e;
			logger.error("transformation failed for class '" + className + "'", e);
		}
		return null;
	}

	private byte[] transformClass(String className) throws Exception {
		CtClass currentClass = pool.get(className);
		if (currentClass.isInterface()) {
			logger.debug("transformation aborted, as class is an interface");
			return null;
		}
		if (currentClass.hasAnnotation(Contract.class)) {
			logger.info("transforming class " + className);
			String contractClassString = new BackdoorAnnotationLoader(currentClass).getClassValue(Contract.class,
					"value");
			targetClassTransformer.transform(currentClass, pool.get(contractClassString));
			contractClasses.add(contractClassString);
			return currentClass.toBytecode();
		} else if (contractClasses.contains(currentClass.getName())) {
			logger.info("transforming contract " + className);
			contractClassTransformer.transform(currentClass.getSuperclass(), currentClass);
			return currentClass.toBytecode();
		}
		return null;
	}

	private void updateClassPath(ClassLoader loader, byte[] classfileBuffer, String className) {
		if (loader != null) {
			pool.insertClassPath(new LoaderClassPath(loader));
		}
		if (classfileBuffer != null) {
			pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
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

}
