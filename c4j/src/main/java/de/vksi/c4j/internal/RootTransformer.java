package de.vksi.c4j.internal;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.configuration.LocalConfigurationCallback;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.configuration.XmlLocalConfiguration;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.contracts.InvolvedTypeInspector;
import de.vksi.c4j.internal.contracts.Transformed;
import de.vksi.c4j.internal.transformer.affected.AffectedClassTransformer;
import de.vksi.c4j.internal.transformer.contract.ContractClassTransformer;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class RootTransformer {
	public static final RootTransformer INSTANCE = new RootTransformer();

	private static final Logger LOGGER = Logger.getLogger(RootTransformer.class);

	AffectedClassTransformer targetClassTransformer;
	ContractClassTransformer contractClassTransformer;

	private final InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	private final XmlConfigurationManager xmlConfiguration = XmlConfigurationManager.INSTANCE;
	private final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

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
				ContractRegistry.INSTANCE.registerExternalContract(xmlLocalConfiguration);
			}

		});
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
		if (ContractRegistry.INSTANCE.isContractClass(affectedClass)) {
			ContractInfo contractInfo = ContractRegistry.INSTANCE.getContractInfo(affectedClass);
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
		ListOrderedSet<ContractInfo> contracts = ContractRegistry.INSTANCE.getContractsForTypes(involvedTypes,
				affectedClass);
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
