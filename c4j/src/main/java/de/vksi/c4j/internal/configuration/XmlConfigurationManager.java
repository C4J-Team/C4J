package de.vksi.c4j.internal.configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;

public class XmlConfigurationManager {
	public static final XmlConfigurationManager INSTANCE = new XmlConfigurationManager();

	public static final String C4J_LOCAL_XML = "c4j-local.xml";
	public static final String C4J_GLOBAL_XML = "c4j-global.xml";
	private Map<String, XmlLocalConfiguration> rootPackageToConfiguration = new HashMap<String, XmlLocalConfiguration>();
	private XmlLocalConfiguration defaultLocalConfiguration;
	private XmlGlobalConfiguration globalConfiguration;

	private Set<URL> localConfigurationsPaths = new HashSet<URL>();
	private URL globalConfigurationPath;

	private JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();
	private Logger logger = Logger.getLogger(XmlConfigurationManager.class);
	private final Set<LocalConfigurationCallback> localConfigurationCallbacks = new HashSet<LocalConfigurationCallback>();

	private XmlConfigurationManager() {
		try {
			initDefaultLocalConfiguration();
			initDefaultGlobalConfiguration();
		} catch (Exception e) {
			logger.fatal("Exception when building default configurations.", e);
		}
	}

	private void initDefaultGlobalConfiguration() throws JAXBException {
		C4JGlobal globalXmlConfiguration = new C4JGlobal();
		jaxbUnmarshaller.setDefaultValues(globalXmlConfiguration);
		globalConfiguration = new XmlGlobalConfiguration(globalXmlConfiguration);
	}

	private void initDefaultLocalConfiguration() throws Exception {
		Configuration localXmlConfiguration = new Configuration();
		jaxbUnmarshaller.setDefaultValues(localXmlConfiguration);
		defaultLocalConfiguration = createLocalConfiguration(localXmlConfiguration, ClassLoader.getSystemClassLoader());
	}

	private XmlLocalConfiguration createLocalConfiguration(Configuration localXmlConfiguration, ClassLoader classLoader)
			throws Exception {
		XmlLocalConfiguration xmlLocalConfiguration = new XmlLocalConfiguration(localXmlConfiguration, classLoader);
		for (LocalConfigurationCallback callback : localConfigurationCallbacks) {
			callback.scanExternalContracts(xmlLocalConfiguration);
		}
		return xmlLocalConfiguration;
	}

	public void registerAndFeedLocalConfigurationCallback(LocalConfigurationCallback localConfigurationCallback)
			throws Exception {
		localConfigurationCallbacks.add(localConfigurationCallback);
		localConfigurationCallback.scanExternalContracts(defaultLocalConfiguration);
		for (XmlLocalConfiguration xmlLocalConfiguration : rootPackageToConfiguration.values()) {
			localConfigurationCallback.scanExternalContracts(xmlLocalConfiguration);
		}
	}

	public void registerClassLoader(ClassLoader classLoader) throws Exception {
		registerLocalConfigs(classLoader);
		registerGlobalConfig(classLoader);
	}

	private void registerGlobalConfig(ClassLoader classLoader) throws Exception {
		URL globalConfigUrl = classLoader.getResource(C4J_GLOBAL_XML);
		if (globalConfigUrl != null) {
			registerExistingGlobalConfig(globalConfigUrl);
		}
	}

	private void registerExistingGlobalConfig(URL xmlUrl) throws Exception {
		if (globalConfigurationPath != null) {
			if (!globalConfigurationPath.equals(xmlUrl))
				logger.error("Discovered duplicate " + C4J_GLOBAL_XML + " on classpath - ignoring: " + xmlUrl);
			return;
		}
		globalConfiguration = new XmlGlobalConfiguration(jaxbUnmarshaller.unmarshal(xmlUrl.openStream(),
				C4JGlobal.class));
		globalConfigurationPath = xmlUrl;
		logger.info("Loaded global configuration from " + xmlUrl + ".");
	}

	private void registerLocalConfigs(ClassLoader classLoader) throws Exception {
		Enumeration<URL> resources = classLoader.getResources(C4J_LOCAL_XML);
		if (resources == null) {
			return;
		}
		while (resources.hasMoreElements()) {
			registerSingleLocalConfig(resources.nextElement(), classLoader);
		}
	}

	private void registerSingleLocalConfig(URL xmlUrl, ClassLoader classLoader) throws Exception {
		InputStream xmlStream = xmlUrl.openStream();
		if (xmlStream != null && !localConfigurationsPaths.contains(xmlUrl))
			registerExistingLocalConfig(xmlStream, xmlUrl, classLoader);
	}

	private void registerExistingLocalConfig(InputStream xmlStream, URL xmlUrl, ClassLoader classLoader)
			throws Exception {
		C4JLocal localConfig = jaxbUnmarshaller.unmarshal(xmlStream, C4JLocal.class);
		addConfigurations(localConfig, xmlUrl, classLoader);
		localConfigurationsPaths.add(xmlUrl);
		logger.info("Loaded local configuration from " + xmlUrl + ".");
	}

	private void addConfigurations(C4JLocal localConfig, URL xmlUrl, ClassLoader classLoader) throws Exception {
		for (Configuration config : localConfig.getConfiguration()) {
			addConfiguration(createLocalConfiguration(config, classLoader), xmlUrl);
		}
	}

	private void addConfiguration(XmlLocalConfiguration config, URL xmlUrl) {
		for (String rootPackage : config.getRootPackage()) {
			addUniqueConfiguration(config, xmlUrl, rootPackage);
		}
	}

	private void addUniqueConfiguration(XmlLocalConfiguration config, URL xmlUrl, String rootPackage) {
		if (rootPackageToConfiguration.containsKey(rootPackage)) {
			logger.error("Configuration for root-package " + rootPackage
					+ " is already defined, ignoring the configuration in " + xmlUrl);
			return;
		}
		rootPackageToConfiguration.put(rootPackage, config);
	}

	public XmlLocalConfiguration getConfiguration(CtClass clazz) {
		return getConfiguration(clazz.getName());
	}

	public XmlLocalConfiguration getConfiguration(Class<?> clazz) {
		return getConfiguration(clazz.getName());
	}

	public XmlLocalConfiguration getConfiguration(String className) {
		return getMapValueFromClassName(decimateLastPart(className), rootPackageToConfiguration,
				defaultLocalConfiguration);
	}

	public boolean isWithinRootPackages(CtClass clazz) {
		return getConfiguration(clazz) != defaultLocalConfiguration;
	}

	private <T> T getMapValueFromClassName(String classNamePrefix, Map<String, T> map, T defaultValue) {
		if (classNamePrefix.isEmpty()) {
			return defaultValue;
		}
		if (map.containsKey(classNamePrefix)) {
			return map.get(classNamePrefix);
		}
		return getMapValueFromClassName(decimateLastPart(classNamePrefix), map, defaultValue);
	}

	private String decimateLastPart(String classNamePrefix) {
		if (!classNamePrefix.contains(".")) {
			return "";
		}
		return classNamePrefix.substring(0, classNamePrefix.lastIndexOf('.'));
	}

	public XmlGlobalConfiguration getGlobalConfiguration() {
		return globalConfiguration;
	}

	public ContractViolationAction getContractViolationAction(Class<?> clazz) {
		return getMapValueFromClassName(clazz.getName(), globalConfiguration.getContractViolationActions(),
				globalConfiguration.getDefaultContractViolationAction());
	}
}
