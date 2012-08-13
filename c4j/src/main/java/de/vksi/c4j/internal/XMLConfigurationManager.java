package de.vksi.c4j.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.configuration.C4JGlobal;
import de.vksi.c4j.internal.configuration.C4JLocal;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;
import de.vksi.c4j.internal.util.JaxbUnmarshaller;

public class XMLConfigurationManager {
	public static final String C4J_LOCAL_XML = "c4j-local.xml";
	public static final String C4J_GLOBAL_XML = "c4j-global.xml";
	private Map<String, XMLLocalConfiguration> rootPackageToConfiguration = new HashMap<String, XMLLocalConfiguration>();
	private XMLLocalConfiguration defaultLocalConfiguration;
	private C4JGlobal globalConfiguration = new C4JGlobal();

	private Set<URL> localConfigurationsPaths = new HashSet<URL>();
	private URL globalConfigurationPath;

	private JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();
	private Logger logger = Logger.getLogger(XMLConfigurationManager.class);

	public XMLConfigurationManager() {
		try {
			Configuration defaultXmlConfiguration = new Configuration();
			jaxbUnmarshaller.setDefaultValues(defaultXmlConfiguration);
			defaultLocalConfiguration = new XMLLocalConfiguration(defaultXmlConfiguration, ClassLoader
					.getSystemClassLoader());
			jaxbUnmarshaller.setDefaultValues(globalConfiguration);
		} catch (Exception e) {
			logger.fatal("Exception when building default configurations.", e);
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
		globalConfiguration = jaxbUnmarshaller.unmarshal(xmlUrl.openStream(), C4JGlobal.class);
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
			addConfiguration(new XMLLocalConfiguration(config, classLoader), xmlUrl);
		}
	}

	private void addConfiguration(XMLLocalConfiguration config, URL xmlUrl) {
		for (String rootPackage : config.getRootPackage()) {
			addUniqueConfiguration(config, xmlUrl, rootPackage);
		}
	}

	private void addUniqueConfiguration(XMLLocalConfiguration config, URL xmlUrl, String rootPackage) {
		if (rootPackageToConfiguration.containsKey(rootPackage)) {
			logger.error("Configuration for root-package " + rootPackage
					+ " is already defined, ignoring the configuration in " + xmlUrl);
			return;
		}
		rootPackageToConfiguration.put(rootPackage, config);
	}

	public XMLLocalConfiguration getConfiguration(CtClass clazz) {
		return getConfiguration(clazz.getName());
	}

	public XMLLocalConfiguration getConfiguration(Class<?> clazz) {
		return getConfiguration(clazz.getName());
	}

	public XMLLocalConfiguration getConfiguration(String currentPackage) {
		while (currentPackage.lastIndexOf('.') > -1)
			if (rootPackageToConfiguration.containsKey(currentPackage = decimateLastPart(currentPackage)))
				return rootPackageToConfiguration.get(currentPackage);
		return defaultLocalConfiguration;
	}

	private String decimateLastPart(String currentPackage) {
		return currentPackage.substring(0, currentPackage.lastIndexOf('.'));
	}

	public C4JGlobal getGlobalConfiguration() {
		return globalConfiguration;
	}
}
