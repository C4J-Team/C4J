package de.vksi.c4j.internal;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;
import de.vksi.c4j.internal.configuration.C4JPureRegistry;
import de.vksi.c4j.internal.configuration.DefaultPreconditionType;
import de.vksi.c4j.internal.util.JaxbUnmarshaller;

public class XmlLocalConfiguration {

	private final Configuration xmlConfiguration;
	private Set<CtMethod> whitelistMethods = new HashSet<CtMethod>();
	private Set<CtMethod> blacklistMethods = new HashSet<CtMethod>();
	private Logger logger = Logger.getLogger(XmlLocalConfiguration.class);
	private JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();
	private Map<String, String> externalContracts;

	public XmlLocalConfiguration(Configuration xmlConfiguration, ClassLoader classLoader) throws Exception {
		this.xmlConfiguration = xmlConfiguration;
		importPureRegistries(xmlConfiguration, classLoader);
		externalContracts = new ContractPackageScanner(xmlConfiguration.getContractScanPackage(), classLoader)
				.getExternalContracts();
	}

	private void importPureRegistries(Configuration xmlConfiguration, ClassLoader classLoader) throws Exception {
		for (String pureRegistryXml : xmlConfiguration.getPureRegistryImport()) {
			importPureRegistry(classLoader, pureRegistryXml);
		}
	}

	private void importPureRegistry(ClassLoader classLoader, String pureRegistryXml) throws Exception {
		URL pureRegistryUrl = classLoader.getResource(pureRegistryXml);
		if (pureRegistryUrl == null) {
			logger.error("Could not find pure-registry " + pureRegistryXml);
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

	public boolean isStrengtheningPreconditionsAllowed() {
		return xmlConfiguration.isStrengtheningPreconditionsAllowed();
	}

	public List<String> getRootPackage() {
		return xmlConfiguration.getRootPackage();
	}

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<CtMethod> getBlacklistMethods() {
		return blacklistMethods;
	}

	public CtClass getExternalContract(ClassPool pool, CtClass type) throws NotFoundException {
		if (externalContracts.containsKey(type.getName())) {
			return pool.get(externalContracts.get(type.getName()));
		}
		return null;
	}

}
