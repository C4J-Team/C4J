package de.andrena.next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;
import de.andrena.next.Contract;
import de.andrena.next.DefaultConfiguration;
import de.andrena.next.internal.transformer.AffectedClassTransformer;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.transformer.TransformationException;
import de.andrena.next.internal.util.BackdoorAnnotationLoader;
import de.andrena.next.internal.util.ContractRegistry;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.InvolvedTypeInspector;
import de.andrena.next.internal.util.ListOrderedSet;

public class RootTransformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(getClass());
	ClassPool pool = ClassPool.getDefault();

	ContractRegistry contractRegistry = new ContractRegistry();

	AffectedClassTransformer targetClassTransformer = new AffectedClassTransformer(this);
	ContractClassTransformer contractClassTransformer = new ContractClassTransformer(this);

	private static Throwable lastException;

	private ConfigurationManager configuration;

	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	public ClassPool getPool() {
		return pool;
	}

	public ConfigurationManager getConfigurationManager() {
		return configuration;
	}

	public InvolvedTypeInspector getInvolvedTypeInspector() {
		return involvedTypeInspector;
	}

	public RootTransformer(String agentArgs, Instrumentation inst) throws Exception {
		loadConfiguration(agentArgs, inst);
	}

	private void loadConfiguration(String agentArgs, Instrumentation inst) throws Exception {
		if (agentArgs == null || agentArgs.isEmpty()) {
			logger.warn("no configuration given - errors from @Pure are completely disabled. using default configuration.");
			configuration = new ConfigurationManager(new DefaultConfiguration(), pool);
		} else {
			try {
				Class<?> configurationClass = Class.forName(agentArgs);
				configuration = new ConfigurationManager((Configuration) configurationClass.newInstance(), pool);
				checkConfigurationLoadingRootClasses(agentArgs);
				logger.info("loaded configuration from class '" + agentArgs + "'.");
			} catch (Exception e) {
				logger.error("could not load configuration from class '" + agentArgs
						+ "'. using default configuration.", e);
				configuration = new ConfigurationManager(new DefaultConfiguration(), pool);
			}
		}
	}

	private void checkConfigurationLoadingRootClasses(String agentArgs) throws NotFoundException {
		for (String className : configuration.getInvolvedClassNames(pool)) {
			if (configuration.isWithinRootPackages(className)) {
				throw new RuntimeException(
						"classes within root-packages must not be referenced by the configuration. offending class: '"
								+ className + "'.");
			}
		}
	}

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
		if (!affectedClass.hasAnnotation(Transformed.class)) {
			transformClass(affectedClass);
		}
		if (configuration.writeTransformedClass(affectedClass)) {
			affectedClass.writeFile();
		}
		return affectedClass.toBytecode();
	}

	private void transformClass(CtClass affectedClass) throws Exception {
		if (contractRegistry.isContractClass(affectedClass)) {
			ContractInfo contractInfo = contractRegistry.getContractInfo(affectedClass);
			transformContractClass(affectedClass, contractInfo);
		} else {
			transformAffectedClass(affectedClass);
		}
	}

	private void transformAffectedClass(CtClass affectedClass) throws NotFoundException, Exception {
		ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(affectedClass);
		ListOrderedSet<ContractInfo> contracts = transformInvolvedContracts(affectedClass, involvedTypes);
		targetClassTransformer.transform(involvedTypes, contracts, affectedClass);
	}

	private ListOrderedSet<ContractInfo> transformInvolvedContracts(CtClass affectedClass,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, Exception {
		ListOrderedSet<ContractInfo> contracts = getContractsForTypes(involvedTypes);
		for (ContractInfo contract : contracts) {
			for (CtClass contractClass : contract.getAllContractClasses()) {
				if (!contractClass.hasAnnotation(Transformed.class)) {
					transformContractClass(contractClass, contract);
				}
			}
		}
		return contracts;
	}

	private void transformContractClass(CtClass contractClass, ContractInfo contractInfo) throws Exception {
		logger.info("transforming contract " + contractClass.getName());
		contractClassTransformer.transform(contractInfo, contractClass);
	}

	public ListOrderedSet<ContractInfo> getContractsForTypes(ListOrderedSet<CtClass> types) throws NotFoundException {
		ListOrderedSet<ContractInfo> contracts = new ListOrderedSet<ContractInfo>();
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
