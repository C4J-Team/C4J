package de.vksi.c4j.internal.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

public class XmlGlobalConfiguration {
	private final C4JGlobal xmlConfiguration;
	private final Map<String, ContractViolationAction> contractViolationActions = new HashMap<String, ContractViolationAction>();
	private ContractViolationAction defaultContractViolationAction;
	private JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller();

	public XmlGlobalConfiguration(C4JGlobal xmlConfiguration) throws JAXBException {
		this.xmlConfiguration = xmlConfiguration;
		defaultContractViolationAction = new ContractViolationAction();
		jaxbUnmarshaller.setDefaultValues(defaultContractViolationAction);
		initContractViolationActions();
	}

	private void initContractViolationActions() {
		for (JAXBElement<? extends ContractViolationAction> element : xmlConfiguration.getContractViolationAction()
				.getDefaultOrPackageOrClazz()) {
			readContractViolationAction(element);
		}
	}

	private void readContractViolationAction(JAXBElement<? extends ContractViolationAction> element) {
		if (element.getDeclaredType().equals(ContractViolationActionWithName.class)) {
			readContractViolationActionWithName((ContractViolationActionWithName) element.getValue());
		} else {
			readDefaultContractViolationAction(element.getValue());
		}
	}

	private void readContractViolationActionWithName(ContractViolationActionWithName value) {
		contractViolationActions.put(value.getName(), value);
	}

	private void readDefaultContractViolationAction(ContractViolationAction value) {
		defaultContractViolationAction = value;
	}

	public boolean writeTransformedClasses() {
		return xmlConfiguration.getWriteTransformedClasses().isValue();
	}

	public String writeTransformedClassesDirectory() {
		return xmlConfiguration.getWriteTransformedClasses().getDirectory();
	}

	public Map<String, ContractViolationAction> getContractViolationActions() {
		return contractViolationActions;
	}

	public ContractViolationAction getDefaultContractViolationAction() {
		return defaultContractViolationAction;
	}

}
