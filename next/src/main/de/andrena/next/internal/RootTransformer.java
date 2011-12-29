package de.andrena.next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import org.apache.log4j.Logger;

import de.andrena.next.Contract;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.transformer.TargetClassTransformer;
import de.andrena.next.internal.util.BackdoorAnnotationLoader;

public class RootTransformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(getClass());
	ClassPool pool = ClassPool.getDefault();

	TargetClassTransformer targetClassTransformer = new TargetClassTransformer();
	ContractClassTransformer contractClassTransformer = new ContractClassTransformer(pool);

	ContractRegistry contractRegistry = new ContractRegistry();

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

	byte[] transformClass(String className) throws Exception {
		CtClass currentClass = pool.get(className);
		if (currentClass.isInterface()) {
			logger.debug("transformation aborted, as class is an interface");
			return null;
		}
		if (currentClass.hasAnnotation(Contract.class)) {
			logger.info("transforming class " + className);
			String contractClassString = new BackdoorAnnotationLoader(currentClass).getClassValue(Contract.class,
					"value");
			CtClass contractClass = pool.get(contractClassString);
			ContractInfo contractInfo = contractRegistry.registerContract(currentClass, contractClass);
			targetClassTransformer.transform(contractInfo);
			return currentClass.toBytecode();
		} else if (contractRegistry.isContractClass(currentClass)) {
			ContractInfo contractInfo = contractRegistry.getContractInfo(currentClass);
			logger.info("transforming contract " + className);
			contractClassTransformer.transform(contractInfo, currentClass);
			return currentClass.toBytecode();
		}
		return null;
	}

	void updateClassPath(ClassLoader loader, byte[] classfileBuffer, String className) {
		if (loader != null) {
			pool.insertClassPath(new LoaderClassPath(loader));
		}
		if (classfileBuffer != null) {
			pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
		}
	}

}
