package de.vksi.c4j.internal;

import static de.vksi.c4j.internal.util.CollectionsHelper.arrayContains;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.configuration.LocalConfigurationCallback;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.configuration.XmlLocalConfiguration;
import de.vksi.c4j.internal.transformer.AffectedClassTransformer;
import de.vksi.c4j.internal.transformer.ContractClassTransformer;
import de.vksi.c4j.internal.util.ContractRegistry;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.InvolvedTypeInspector;
import de.vksi.c4j.internal.util.ListOrderedSet;

public class RootTransformer {
	public static final RootTransformer INSTANCE = new RootTransformer();

	private static final Logger LOGGER = Logger.getLogger(RootTransformer.class);

	ContractRegistry contractRegistry = new ContractRegistry();

	AffectedClassTransformer targetClassTransformer;
	ContractClassTransformer contractClassTransformer;

	private final InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	private final XmlConfigurationManager xmlConfiguration = XmlConfigurationManager.INSTANCE;
	private final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
	private final Map<XmlLocalConfiguration, Map<String, String>> externalContracts = new HashMap<XmlLocalConfiguration, Map<String, String>>();

	private RootTransformer() {
	}

	public void init() throws Exception {
		targetClassTransformer = new AffectedClassTransformer();
		contractClassTransformer = new ContractClassTransformer();
		loadLogger();
		xmlConfiguration.registerClassLoader(ClassLoader.getSystemClassLoader());
		xmlConfiguration.registerAndFeedLocalConfigurationCallback(new LocalConfigurationCallback() {

			@Override
			public void scanExternalContracts(XmlLocalConfiguration xmlLocalConfiguration) throws Exception {
				externalContracts.put(xmlLocalConfiguration, new ContractPackageScanner(xmlLocalConfiguration
						.getContractScanPackages(), xmlLocalConfiguration.getClassLoader()).getExternalContracts());
			}

		});
	}

	public CtClass getExternalContract(CtClass type, CtClass affectedClass) throws NotFoundException {
		Map<String, String> localExternalContracts = externalContracts.get(xmlConfiguration
				.getConfiguration(affectedClass));
		if (localExternalContracts.containsKey(type.getName())) {
			return ClassFilePool.INSTANCE.getClass(localExternalContracts.get(type.getName()));
		}
		return null;
	}

	private void loadLogger() {
		Enumeration<?> allAppenders = Logger.getRootLogger().getAllAppenders();
		if (!allAppenders.hasMoreElements()) {
			Layout layout = new PatternLayout("C4J %-5p - %m%n");
			Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
			Logger.getRootLogger().setLevel(Level.INFO);
			LOGGER.info("No Appender on RootLogger found, added a new ConsoleAppender on Level INFO.");
		}
	}

	public byte[] transformType(CtClass affectedClass) throws Exception {
		if (affectedClass.isInterface()) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("transformation aborted, as class is an interface");
			}
			return null;
		}
		if (!affectedClass.hasAnnotation(Transformed.class)) {
			transformClass(affectedClass);
		}
		if (xmlConfiguration.getGlobalConfiguration().writeTransformedClasses()) {
			affectedClass.writeFile(xmlConfiguration.getGlobalConfiguration().writeTransformedClassesDirectory());
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
			LOGGER.info(affectedClass.getSimpleName() + " must fulfill contract "
					+ contract.getContractClass().getSimpleName() + " (defined on "
					+ contract.getTargetClass().getSimpleName() + ").");
		}
		targetClassTransformer.transform(involvedTypes, contracts, affectedClass);
	}

	private ListOrderedSet<ContractInfo> transformInvolvedContracts(CtClass affectedClass,
			ListOrderedSet<CtClass> involvedTypes) throws NotFoundException, Exception {
		ListOrderedSet<ContractInfo> contracts = getContractsForTypes(involvedTypes, affectedClass);
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

	public ListOrderedSet<ContractInfo> getContractsForTypes(ListOrderedSet<CtClass> types, CtClass affectedClass)
			throws NotFoundException {
		ListOrderedSet<ContractInfo> contracts = new ListOrderedSet<ContractInfo>();
		for (CtClass type : types) {
			CtClass externalContract = getExternalContract(type, affectedClass);
			if (type.hasAnnotation(ContractReference.class) || externalContract != null) {
				if (contractRegistry.hasRegisteredContract(type)) {
					contracts.add(contractRegistry.getContractInfoForTargetClass(type));
				} else {
					CtClass contractClass = decideContractForType(type, externalContract);
					if (verifyContract(type, contractClass, affectedClass)) {
						contracts.add(contractRegistry.registerContract(type, contractClass));
					}
				}
			}
		}
		return contracts;
	}

	private CtClass decideContractForType(CtClass type, CtClass externalContract) throws NotFoundException {
		if (type.hasAnnotation(ContractReference.class)) {
			return ClassFilePool.INSTANCE.getClassFromAnnotationValue(type, ContractReference.class, "value");
		}
		return externalContract;
	}

	private boolean verifyContract(CtClass targetClass, CtClass contractClass, CtClass affectedClass)
			throws NotFoundException {
		if (contractClass.hasAnnotation(Transformed.class)) {
			LOGGER.error("Ignoring contract class " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as it has been loaded before the target type was loaded.");
			return false;
		}
		if (contractClass.isInterface()) {
			LOGGER.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is an interface.");
			return false;
		}
		if (contractClass.equals(targetClass)) {
			LOGGER.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is the same as the target class.");
			return false;
		}
		if (contractClass.equals(affectedClass)) {
			LOGGER.error("Class " + contractClass.getSimpleName()
					+ " cannot be its own contract-class. Try explicitly marking " + contractClass.getSimpleName()
					+ " with @Contract.");
			return false;
		}
		warnContractNotInheritingFromTarget(targetClass, contractClass);
		return true;
	}

	private void warnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass)
			throws NotFoundException {
		if (targetClass.isInterface()) {
			if (!arrayContains(contractClass.getInterfaces(), targetClass)) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		} else {
			if (!contractClass.getSuperclass().equals(targetClass) && !Modifier.isFinal(targetClass.getModifiers())) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		}
	}

	private void logWarnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass) {
		LOGGER.warn("Contract type " + contractClass.getSimpleName()
				+ " does not inherit from its non-final target type " + targetClass.getSimpleName() + ".");
	}

	public void updateClassPath(ClassLoader loader, byte[] classfileBuffer, String className) {
		if (loader != null && !classLoaders.contains(loader)) {
			classLoaders.add(loader);
			addClassLoader(loader);
		}
		if (classfileBuffer != null) {
			ClassFilePool.INSTANCE.addClassFile(classfileBuffer, className);
		}
	}

	private void addClassLoader(ClassLoader loader) {
		ClassFilePool.INSTANCE.addClassLoader(loader);
		try {
			xmlConfiguration.registerClassLoader(loader);
		} catch (Exception e) {
			LOGGER.error("Could not add ClassLoader " + loader.getClass().getName() + " to configuration.", e);
		}
	}

	public XmlConfigurationManager getXmlConfiguration() {
		return xmlConfiguration;
	}

}
