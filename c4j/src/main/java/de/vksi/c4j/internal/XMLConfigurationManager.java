package de.vksi.c4j.internal;

import java.io.IOException;
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

import de.vksi.c4j.internal.configuration.C4JGlobal;
import de.vksi.c4j.internal.configuration.C4JLocal;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;
import de.vksi.c4j.internal.util.JaxbUnmarshaller;

public class XMLConfigurationManager {
	public static final String C4J_LOCAL_XML = "c4j-local.xml";
	public static final String C4J_GLOBAL_XML = "c4j-global.xml";
	private Map<String, Configuration> rootPackageToConfiguration = new HashMap<String, Configuration>();
	private Configuration defaultLocalConfiguration = new Configuration();
	private C4JGlobal globalConfiguration = new C4JGlobal();

	private Set<URL> localConfigurationsPaths = new HashSet<URL>();
	private URL globalConfigurationPath;

	private JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();
	private Logger logger = Logger.getLogger(XMLConfigurationManager.class);

	public XMLConfigurationManager() {
		try {
			jaxbUnmarshaller.setDefaultValues(defaultLocalConfiguration);
			jaxbUnmarshaller.setDefaultValues(globalConfiguration);
		} catch (JAXBException e) {
			logger.fatal("Exception when building default configurations.", e);
		}
	}

	public void registerClassLoader(ClassLoader classLoader) throws Exception {
		registerLocalConfigs(classLoader);
		registerGlobalConfig(classLoader);
	}

	private void registerGlobalConfig(ClassLoader classLoader) throws JAXBException {
		InputStream xmlStream = classLoader.getResourceAsStream(C4J_GLOBAL_XML);
		if (xmlStream != null)
			registerExistingGlobalConfig(xmlStream, classLoader.getResource(C4J_GLOBAL_XML));
	}

	private void registerExistingGlobalConfig(InputStream xmlStream, URL xmlUrl) throws JAXBException {
		if (globalConfigurationPath != null) {
			if (!globalConfigurationPath.equals(xmlUrl))
				logger.error("Discovered duplicate " + C4J_GLOBAL_XML + " on classpath - ignoring: " + xmlUrl);
			return;
		}
		globalConfiguration = jaxbUnmarshaller.unmarshal(xmlStream, C4JGlobal.class);
		globalConfigurationPath = xmlUrl;
		logger.info("Loaded global configuration from " + xmlUrl + ".");
	}

	private void registerLocalConfigs(ClassLoader classLoader) throws Exception {
		Enumeration<URL> resources = classLoader.getResources(C4J_LOCAL_XML);
		if (resources == null) {
			return;
		}
		while (resources.hasMoreElements()) {
			registerSingleLocalConfig(resources.nextElement());
		}
	}

	private void registerSingleLocalConfig(URL xmlUrl) throws IOException, JAXBException {
		InputStream xmlStream = xmlUrl.openStream();
		if (xmlStream != null && !localConfigurationsPaths.contains(xmlUrl))
			registerExistingLocalConfig(xmlStream, xmlUrl);
	}

	private void registerExistingLocalConfig(InputStream xmlStream, URL xmlUrl) throws JAXBException {
		C4JLocal localConfig = jaxbUnmarshaller.unmarshal(xmlStream, C4JLocal.class);
		addConfigurations(localConfig, xmlUrl);
		localConfigurationsPaths.add(xmlUrl);
		logger.info("Loaded local configuration from " + xmlUrl + ".");
	}

	private void addConfigurations(C4JLocal localConfig, URL xmlUrl) {
		for (Configuration config : localConfig.getConfiguration()) {
			addConfiguration(config, xmlUrl);
		}
	}

	private void addConfiguration(Configuration config, URL xmlUrl) {
		for (String rootPackage : config.getRootPackage()) {
			addUniqueConfiguration(config, xmlUrl, rootPackage);
		}
	}

	private void addUniqueConfiguration(Configuration config, URL xmlUrl, String rootPackage) {
		if (rootPackageToConfiguration.containsKey(rootPackage)) {
			logger.error("Configuration for root-package " + rootPackage
					+ " is already defined, ignoring the configuration in " + xmlUrl);
			return;
		}
		rootPackageToConfiguration.put(rootPackage, config);
	}

	public Configuration getConfiguration(CtClass clazz) {
		return getConfiguration(clazz.getName());
	}

	public Configuration getConfiguration(Class<?> clazz) {
		return getConfiguration(clazz.getName());
	}

	public Configuration getConfiguration(String currentPackage) {
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
