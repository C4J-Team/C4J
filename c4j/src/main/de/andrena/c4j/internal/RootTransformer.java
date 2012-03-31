package de.andrena.c4j.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Enumeration;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.andrena.c4j.Configuration;
import de.andrena.c4j.ContractReference;
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
		loadLogger();
		loadConfiguration(agentArgs);
	}

	private void loadLogger() {
		Enumeration<?> allAppenders = Logger.getRootLogger().getAllAppenders();
		if (!allAppenders.hasMoreElements()) {
			Layout layout = new PatternLayout("C4J %-5p - %m%n");
			Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("No Appender on RootLogger found, added a new ConsoleAppender on Level INFO.");
		}
	}

	private void loadConfiguration(String agentArgs) throws Exception {
		if (agentArgs == null || agentArgs.isEmpty()) {
			logger.info("No configuration given, using DefaultConfiguration.");
			configuration = new ConfigurationManager(new DefaultConfiguration(), pool);
		} else {
			try {
				Class<?> configurationClass = Class.forName(agentArgs, true, new LocalClassLoader(getClass()
						.getClassLoader()));
				configuration = new ConfigurationManager((Configuration) configurationClass.newInstance(), pool);
				logger.info("Loaded configuration from class '" + agentArgs + "'.");
			} catch (Exception e) {
				logger.error("Could not load configuration from class '" + agentArgs
						+ "'. Using DefaultConfiguration.", e);
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
		logger.trace("transformation started for class " + className);
		try {
			updateClassPath(loader, classfileBuffer, className);
			return transformClass(className);
		} catch (Exception e) {
			lastException = e;
			logger.fatal("Transformation failed for class '" + className + "'.", e);
		}
		return null;
	}

	byte[] transformClass(String className) throws Exception {
		CtClass affectedClass = pool.get(className);
		if (affectedClass.isInterface()) {
			logger.trace("transformation aborted, as class is an interface");
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
		for (ContractInfo contract : contracts) {
			logger.info(affectedClass.getSimpleName() + " must fulfill contract "
					+ contract.getContractClass().getSimpleName() + " (defined on "
					+ contract.getTargetClass().getSimpleName() + ").");
		}
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
		contractClassTransformer.transform(contractInfo, contractClass);
	}

	public ListOrderedSet<ContractInfo> getContractsForTypes(ListOrderedSet<CtClass> types) throws NotFoundException {
		ListOrderedSet<ContractInfo> contracts = new ListOrderedSet<ContractInfo>();
		for (CtClass type : types) {
			CtClass externalContract = configuration.getConfiguration(type).getExternalContract(pool, type);
			if (type.hasAnnotation(ContractReference.class) || externalContract != null) {
				if (contractRegistry.hasRegisteredContract(type)) {
					contracts.add(contractRegistry.getContractInfoForTargetClass(type));
				} else if (type.hasAnnotation(ContractReference.class)) {
					String contractClassString = new BackdoorAnnotationLoader(type).getClassValue(
							ContractReference.class,
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
