package de.andrena.c4j.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.c4j.Configuration;
import de.andrena.c4j.Contract;
import de.andrena.c4j.DefaultConfiguration;
import de.andrena.c4j.internal.transformer.AffectedClassTransformer;
import de.andrena.c4j.internal.transformer.ContractClassTransformer;
import de.andrena.c4j.internal.util.BackdoorAnnotationLoader;
import de.andrena.c4j.internal.util.ContractRegistry;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.InvolvedTypeInspector;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.LocalClassLoader;

public class RootTransformer implements ClassFileTransformer {
	public static final RootTransformer INSTANCE = new RootTransformer();

	private Logger logger = Logger.getLogger(getClass());
	ClassPool pool = ClassPool.getDefault();

	ContractRegistry contractRegistry = new ContractRegistry();

	AffectedClassTransformer targetClassTransformer;
	ContractClassTransformer contractClassTransformer;

	private static Throwable lastException;

	private ConfigurationManager configuration;

	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	public ClassPool getPool() {
		return pool;
	}

	public ConfigurationManager getConfigurationManager() {
		return configuration;
	}

	private RootTransformer() {
	}

	public void init(String agentArgs) throws Exception {
		targetClassTransformer = new AffectedClassTransformer();
		contractClassTransformer = new ContractClassTransformer();
		loadConfiguration(agentArgs);
	}

	private void loadConfiguration(String agentArgs) throws Exception {
		if (agentArgs == null || agentArgs.isEmpty()) {
			logger.warn("no configuration given - errors from @Pure are completely disabled. using default configuration.");
			configuration = new ConfigurationManager(new DefaultConfiguration(), pool);
		} else {
			try {
				Class<?> configurationClass = Class.forName(agentArgs, true, new LocalClassLoader(getClass()
						.getClassLoader()));
				configuration = new ConfigurationManager((Configuration) configurationClass.newInstance(), pool);
				logger.info("loaded configuration from class '" + agentArgs + "'.");
			} catch (Exception e) {
				logger.error("could not load configuration from class '" + agentArgs
						+ "'. using default configuration.", e);
				configuration = new ConfigurationManager(new DefaultConfiguration(), pool);
			}
		}
	}

	public static Throwable getLastException() {
		return lastException;
	}

	public static void resetLastException() {
		lastException = null;
	}

	@Override
	public byte[] transform(ClassLoader loader, String classNameWithSlashes, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		String className = classNameWithSlashes.replace('/', '.');
		logger.debug("transformation started for class " + className);
		try {
			updateClassPath(loader, classfileBuffer, className);
			return transformClass(className);
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
		if (configuration.getConfiguration(affectedClass).writeTransformedClasses()) {
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
			CtClass externalContract = configuration.getConfiguration(type).getExternalContract(pool, type);
			if (type.hasAnnotation(Contract.class) || externalContract != null) {
				if (contractRegistry.hasRegisteredContract(type)) {
					contracts.add(contractRegistry.getContractInfoForTargetClass(type));
				} else if (type.hasAnnotation(Contract.class)) {
					String contractClassString = new BackdoorAnnotationLoader(type).getClassValue(Contract.class,
							"value");
					CtClass contractClass = pool.get(contractClassString);
					contracts.add(contractRegistry.registerContract(type, contractClass));
				} else {
					contracts.add(contractRegistry.registerContract(type, externalContract));
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
