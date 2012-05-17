package de.andrena.c4j.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
import de.andrena.c4j.internal.util.BackdoorAnnotationLoader;
import de.andrena.c4j.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private static final String FILE_EXT_JAR = ".jar";
	private static final String FILE_EXT_CLASS = ".class";
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
		loadExternalContractsAsStrings();
		searchContractsDirectory();
	}

	private void searchContractsDirectory() throws Exception {
		if (configuration.getContractsDirectory() != null) {
			if (configuration.getContractsDirectory().isDirectory()) {
				logger.info("Using ContractsDirectory " + configuration.getContractsDirectory().getCanonicalPath()
						+ " from Configuration " + getConfigurationClass().getSimpleName() + ".");
				searchContracts(configuration.getContractsDirectory());
				return;
			}
			if (configuration.getContractsDirectory().isFile()
					&& configuration.getContractsDirectory().getName().endsWith(FILE_EXT_JAR)) {
				logger.info("Using ContractsDirectory " + configuration.getContractsDirectory().getCanonicalPath()
						+ " from Configuration " + getConfigurationClass().getSimpleName() + ".");
				searchContracts(new JarFile(configuration.getContractsDirectory()));
				return;
			}
			logger.error("ContractsDirectory " + configuration.getContractsDirectory().getCanonicalPath()
					+ " does not exist or is not a directory and will not be searched.");
		}
	}

	private void searchContracts(JarFile jarFile) throws Exception {
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry currentEntry = entries.nextElement();
			if (!currentEntry.isDirectory() && currentEntry.getName().endsWith(FILE_EXT_CLASS)) {
				checkClassFileForContract(jarFile.getInputStream(currentEntry));
			}
		}
	}

	private void searchContracts(File directory) throws Exception {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				searchContracts(file);
			} else if (file.getName().endsWith(FILE_EXT_CLASS)) {
				checkClassFileForContract(new FileInputStream(file));
			}
		}
	}

	private void checkClassFileForContract(InputStream inputStream) throws Exception {
		CtClass loadedClass = RootTransformer.INSTANCE.getPool().makeClassIfNew(inputStream);
		if (loadedClass.hasAnnotation(Contract.class)) {
			String targetFromAnnotation = new BackdoorAnnotationLoader(loadedClass).getClassValue(Contract.class,
					"forTarget");
			String contractClass = loadedClass.getName();
			if (targetFromAnnotation != null && !targetFromAnnotation.equals(InheritedType.class.getName())) {
				addExternalContract(targetFromAnnotation, contractClass);
			} else {
				if (!loadedClass.getClassFile().getSuperclass().equals(Object.class.getName())) {
					addExternalContract(loadedClass.getClassFile().getSuperclass(), contractClass);
				} else if (loadedClass.getClassFile().getInterfaces().length == 1) {
					addExternalContract(loadedClass.getClassFile().getInterfaces()[0], contractClass);
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

	private void addExternalContract(String targetClass, String contractClass) {
		logger.info("Found external Contract " + contractClass + " for target class " + targetClass);
		externalContracts.put(targetClass, contractClass);
	}

	private void stringifyExternalContracts() {
		for (Class<?> targetClass : configuration.getExternalContracts().keySet()) {
			addExternalContract(targetClass.getName(), configuration.getExternalContracts().get(targetClass)
					.getName());
		}
	}

	private void loadExternalContractsAsStrings() {
		for (String targetClass : configuration.getExternalContractsAsStrings().keySet()) {
			addExternalContract(targetClass, configuration.getExternalContractsAsStrings().get(targetClass));
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
