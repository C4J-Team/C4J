package de.vksi.c4j.internal.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CtMethod;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration.ContractScanPackage;

public class XmlLocalConfiguration {
	private static final Logger LOGGER = Logger.getLogger(XmlLocalConfiguration.class);

	private final Configuration xmlConfiguration;
	private final Set<CtMethod> whitelistMethods = new HashSet<CtMethod>();
	private final Set<CtMethod> blacklistMethods = new HashSet<CtMethod>();
	private final JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();
	private final ClassLoader classLoader;

	public XmlLocalConfiguration(Configuration xmlConfiguration, ClassLoader classLoader) throws Exception {
		this.xmlConfiguration = xmlConfiguration;
		this.classLoader = classLoader;
		importPureRegistries(xmlConfiguration);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	private void importPureRegistries(Configuration xmlConfiguration) throws Exception {
		for (String pureRegistryXml : xmlConfiguration.getPureRegistryImport()) {
			importPureRegistry(pureRegistryXml);
		}
	}

	private void importPureRegistry(String pureRegistryXml) throws Exception {
		URL pureRegistryUrl = classLoader.getResource(pureRegistryXml);
		if (pureRegistryUrl == null) {
			LOGGER.error("Could not find pure-registry " + pureRegistryXml);
			return;
		}
		importExistingPureRegistry(pureRegistryUrl);
	}

	private void importExistingPureRegistry(URL pureRegistryUrl) throws JAXBException, IOException {
		PureRegistryImporter pureRegistryImporter = new PureRegistryImporter(jaxbUnmarshaller.unmarshal(pureRegistryUrl
				.openStream(), C4JPureRegistry.class), pureRegistryUrl);
		whitelistMethods.addAll(pureRegistryImporter.getWhitelistMethods());
		blacklistMethods.addAll(pureRegistryImporter.getBlacklistMethods());
	}

	public boolean isPureValidate() {
		return xmlConfiguration.isPureValidate();
	}

	public DefaultPreconditionType getDefaultPrecondition() {
		return xmlConfiguration.getDefaultPrecondition();
	}

	public boolean isPureSkipInvariants() {
		return xmlConfiguration.isPureSkipInvariants();
	}

	public List<String> getRootPackage() {
		return xmlConfiguration.getRootPackage();
	}

	public List<ContractScanPackage> getContractScanPackages() {
		return xmlConfiguration.getContractScanPackage();
	}

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<CtMethod> getBlacklistMethods() {
		return blacklistMethods;
	}

}
