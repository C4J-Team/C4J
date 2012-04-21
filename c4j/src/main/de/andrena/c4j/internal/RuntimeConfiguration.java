package de.andrena.c4j.internal;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.c4j.Configuration;
import de.andrena.c4j.Configuration.ContractViolationAction;
import de.andrena.c4j.Configuration.DefaultPreCondition;
import de.andrena.c4j.Configuration.PureBehavior;
import de.andrena.c4j.Contract;
import de.andrena.c4j.Contract.InheritedType;
import de.andrena.c4j.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private Set<CtMethod> whitelistMethods;
	private Configuration configuration;
	private Map<String, String> externalContracts = new HashMap<String, String>();
	private Set<CtMethod> blacklistMethods;
	private Logger logger = Logger.getLogger(RuntimeConfiguration.class);

	public RuntimeConfiguration(Configuration configuration, WhitelistConverter whitelistConverter) throws Exception {
		this.configuration = configuration;
		whitelistMethods = whitelistConverter.convertWhitelist(configuration.getPureRegistry().getPureMethods());
		blacklistMethods = whitelistConverter.convertWhitelist(configuration.getPureRegistry().getUnpureMethods());
		stringifyExternalContracts();
		searchContractsDirectory();
	}

	private void searchContractsDirectory() throws Exception {
		if (configuration.getContractsDirectory() != null) {
			if (!configuration.getContractsDirectory().isDirectory()) {
				logger.error("ContractsDirectory " + configuration.getContractsDirectory().getCanonicalPath()
						+ " does not exist or is not a directory and will not be searched.");
				return;
			}
			logger.info("Using ContractsDirectory " + configuration.getContractsDirectory().getCanonicalPath()
					+ " from Configuration " + getConfigurationClass().getSimpleName() + ".");
			searchContracts(configuration.getContractsDirectory());
		}
	}

	private void searchContracts(File directory) throws Exception {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				searchContracts(file);
			} else if (file.getName().endsWith(".class")) {
				checkClassFileForContract(file);
			}
		}
	}

	private void checkClassFileForContract(File file) throws Exception {
		CtClass loadedClass = RootTransformer.INSTANCE.getPool().makeClassIfNew(new FileInputStream(file));
		if (loadedClass.hasAnnotation(Contract.class)) {
			Class<?> targetFromAnnotation = ((Contract) loadedClass.getAnnotation(Contract.class)).forTarget();
			if (!targetFromAnnotation.equals(InheritedType.class)) {
				externalContracts.put(targetFromAnnotation.getName(), loadedClass.getName());
			} else {
				if (!loadedClass.getSuperclass().equals(RootTransformer.INSTANCE.getPool().get(Object.class.getName()))) {
					externalContracts.put(loadedClass.getSuperclass().getName(), loadedClass.getName());
				} else if (loadedClass.getInterfaces().length == 1) {
					externalContracts.put(loadedClass.getInterfaces()[0].getName(), loadedClass.getName());
				} else {
					logger.error("Contract "
							+ loadedClass.getSimpleName()
							+ " was found in ContractsDirectory, but could not be mapped to a target class. "
							+ "Extend from the target class or implement the target interface (while not implementing any other interfaces) "
							+ "or simply declare the target class within the @Contract annotation.");
				}
			}
		}
	}

	private void stringifyExternalContracts() {
		for (Class<?> targetClass : configuration.getExternalContracts().keySet()) {
			externalContracts.put(targetClass.getName(), configuration.getExternalContracts().get(targetClass)
					.getName());
		}
	}

	public Class<?> getConfigurationClass() {
		return configuration.getClass();
	}

	public Set<String> getInvolvedClassNames(ClassPool pool) throws NotFoundException {
		@SuppressWarnings("unchecked")
		Set<String> classNamesWithSlashes = pool.get(getConfigurationClass().getName()).getClassFile().getConstPool()
				.getClassNames();
		Set<String> involvedClassNames = new HashSet<String>();
		for (String classNameWithSlashes : classNamesWithSlashes) {
			involvedClassNames.add(classNameWithSlashes.replace('/', '.'));
		}
		return involvedClassNames;
	}

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<CtMethod> getBlacklistMethods() {
		return blacklistMethods;
	}

	public Set<String> getRootPackages() {
		return configuration.getRootPackages();
	}

	public boolean writeTransformedClasses() {
		return configuration.writeTransformedClasses();
	}

	public DefaultPreCondition getDefaultPreCondition() {
		return configuration.getDefaultPreCondition();
	}

	public boolean isStrengtheningPreConditionAllowed() {
		return configuration.isStrengtheningPreConditionAllowed();
	}

	public Set<ContractViolationAction> getContractViolationActions() {
		return configuration.getContractViolationActions();
	}

	public CtClass getExternalContract(ClassPool pool, CtClass type) throws NotFoundException {
		if (externalContracts.containsKey(type.getName())) {
			return pool.get(externalContracts.get(type.getName()));
		}
		return null;
	}

	public Set<PureBehavior> getPureBehaviors() {
		return configuration.getPureBehaviors();
	}
}
