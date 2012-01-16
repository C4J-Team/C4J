package de.andrena.next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Contract;
import de.andrena.next.internal.transformer.AffectedClassTransformer;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.transformer.TransformationException;
import de.andrena.next.internal.util.BackdoorAnnotationLoader;
import de.andrena.next.internal.util.ContractRegistry;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class RootTransformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(getClass());
	ClassPool pool = ClassPool.getDefault();

	ContractRegistry contractRegistry = new ContractRegistry();

	AffectedClassTransformer targetClassTransformer = new AffectedClassTransformer();
	ContractClassTransformer contractClassTransformer = new ContractClassTransformer(pool);

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
			logger.fatal("transformation failed for class '" + className + "'", e);
		}
		return null;
	}

	byte[] transformClass(String className) throws Exception {
		CtClass affectedClass = pool.get(className);
		if (affectedClass.isInterface()) {
			logger.debug("transformation aborted, as class is an interface");
			return null;
		}
		if (contractRegistry.isContractClass(affectedClass)) {
			ContractInfo contractInfo = contractRegistry.getContractInfo(affectedClass);
			logger.info("transforming contract " + className);
			contractClassTransformer.transform(contractInfo, affectedClass);
			return affectedClass.toBytecode();
		}
		Set<CtClass> involvedTypes = getInvolvedTypes(affectedClass);
		targetClassTransformer.transform(involvedTypes, getContractsForTypes(involvedTypes), affectedClass);
		return affectedClass.toBytecode();
	}

	Set<CtClass> getInvolvedTypes(CtClass type) throws NotFoundException {
		Set<CtClass> inheritedTypes = new HashSet<CtClass>();
		inheritedTypes.add(type);
		if (type.getSuperclass() != null) {
			inheritedTypes.addAll(getInvolvedTypes(type.getSuperclass()));
		}
		for (CtClass interfaze : type.getInterfaces()) {
			inheritedTypes.addAll(getInvolvedTypes(interfaze));
		}
		return inheritedTypes;
	}

	private Set<ContractInfo> getContractsForTypes(Set<CtClass> types) throws NotFoundException {
		Set<ContractInfo> contracts = new HashSet<ContractInfo>();
		for (CtClass type : types) {
			if (type.hasAnnotation(Contract.class)) {
				if (contractRegistry.hasRegisteredContract(type)) {
					contracts.add(contractRegistry.getContractInfoForTargetClass(type));
				} else {
					String contractClassString = new BackdoorAnnotationLoader(type).getClassValue(Contract.class,
							"value");
					CtClass contractClass = pool.get(contractClassString);
					contracts.add(contractRegistry.registerContract(type, contractClass));
				}
			}
		}
		return contracts;
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
