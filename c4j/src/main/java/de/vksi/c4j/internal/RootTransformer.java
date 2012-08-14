package de.vksi.c4j.internal;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.transformer.AffectedClassTransformer;
import de.vksi.c4j.internal.transformer.ContractClassTransformer;
import de.vksi.c4j.internal.util.BackdoorAnnotationLoader;
import de.vksi.c4j.internal.util.CollectionsHelper;
import de.vksi.c4j.internal.util.ContractRegistry;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.InvolvedTypeInspector;
import de.vksi.c4j.internal.util.ListOrderedSet;

public class RootTransformer {
	public static final RootTransformer INSTANCE = new RootTransformer();

	private Logger logger = Logger.getLogger(RootTransformer.class);
	ClassPool pool = ClassPool.getDefault();

	ContractRegistry contractRegistry = new ContractRegistry();

	AffectedClassTransformer targetClassTransformer;
	ContractClassTransformer contractClassTransformer;

	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	private CollectionsHelper collectionsHelper = new CollectionsHelper();

	private XmlConfigurationManager xmlConfiguration;
	private Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

	public ClassPool getPool() {
		return pool;
	}

	private RootTransformer() {
	}

	public void init() throws Exception {
		targetClassTransformer = new AffectedClassTransformer();
		contractClassTransformer = new ContractClassTransformer();
		loadLogger();
		xmlConfiguration = new XmlConfigurationManager();
		xmlConfiguration.registerClassLoader(ClassLoader.getSystemClassLoader());
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

	public byte[] transformType(CtClass affectedClass) throws Exception {
		if (affectedClass.isInterface()) {
			if (logger.isTraceEnabled()) {
				logger.trace("transformation aborted, as class is an interface");
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
			logger.info(affectedClass.getSimpleName() + " must fulfill contract "
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
			CtClass externalContract = xmlConfiguration.getConfiguration(affectedClass).getExternalContract(pool, type);
			if (type.hasAnnotation(ContractReference.class) || externalContract != null) {
				if (contractRegistry.hasRegisteredContract(type)) {
					contracts.add(contractRegistry.getContractInfoForTargetClass(type));
				} else {
					verifyRegisterAndAddContract(contracts, type, externalContract);
				}
			}
		}
		return contracts;
	}

	private void verifyRegisterAndAddContract(ListOrderedSet<ContractInfo> contracts, CtClass type,
			CtClass externalContract) throws NotFoundException {
		CtClass contractClass = decideContractForType(type, externalContract);
		if (verifyContract(type, contractClass)) {
			contracts.add(contractRegistry.registerContract(type, contractClass));
		}
	}

	private CtClass decideContractForType(CtClass type, CtClass externalContract) throws NotFoundException {
		if (type.hasAnnotation(ContractReference.class)) {
			String contractClassString = new BackdoorAnnotationLoader(type).getClassValue(ContractReference.class,
					"value");
			return pool.get(contractClassString);
		}
		return externalContract;
	}

	private boolean verifyContract(CtClass targetClass, CtClass contractClass) throws NotFoundException {
		if (contractClass.hasAnnotation(Transformed.class)) {
			logger.error("Ignoring contract class " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as it has been loaded before the target type was loaded.");
			return false;
		}
		if (contractClass.isInterface()) {
			logger.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is an interface.");
			return false;
		}
		if (contractClass.equals(targetClass)) {
			logger.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is the same as the target class.");
			return false;
		}
		warnContractNotInheritingFromTarget(targetClass, contractClass);
		return true;
	}

	private void warnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass)
			throws NotFoundException {
		if (targetClass.isInterface()) {
			if (!collectionsHelper.arrayContains(contractClass.getInterfaces(), targetClass)) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		} else {
			if (!contractClass.getSuperclass().equals(targetClass) && !Modifier.isFinal(targetClass.getModifiers())) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		}
	}

	private void logWarnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass) {
		logger.warn("Contract type " + contractClass.getSimpleName()
				+ " does not inherit from its non-final target type " + targetClass.getSimpleName() + ".");
	}

	public void updateClassPath(ClassLoader loader, byte[] classfileBuffer, String className) {
		if (loader != null && !classLoaders.contains(loader)) {
			classLoaders.add(loader);
			addClassLoader(loader);
		}
		if (classfileBuffer != null) {
			addClassFile(classfileBuffer, className);
		}
	}

	private void addClassFile(byte[] classfileBuffer, String className) {
		if (logger.isTraceEnabled()) {
			logger.trace("updating classpath with classfileBuffer for class " + className);
		}
		pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
	}

	private void addClassLoader(ClassLoader loader) {
		if (logger.isTraceEnabled()) {
			logger.trace("updating classpath with loader " + loader.getClass() + ", parent " + loader.getParent());
		}
		pool.insertClassPath(new LoaderClassPath(loader));
		try {
			xmlConfiguration.registerClassLoader(loader);
		} catch (Exception e) {
			logger.error("Could not add ClassLoader " + loader.getClass().getName() + " to configuration.", e);
		}
	}

	public XmlConfigurationManager getXmlConfiguration() {
		return xmlConfiguration;
	}

}
