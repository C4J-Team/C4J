package de.vksi.c4j.internal;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import de.vksi.c4j.internal.configuration.C4JGlobal;
import de.vksi.c4j.internal.configuration.ContractViolationAction;
import de.vksi.c4j.internal.configuration.ContractViolationActionWithName;

public class GlobalConfiguration {
	private C4JGlobal origin;
	private ContractViolationAction defaultContractViolationAction;
	private Map<String, ContractViolationAction> contractViolationActions = new HashMap<String, ContractViolationAction>();

	public GlobalConfiguration(C4JGlobal origin) {
		this.origin = origin;
		readContractViolationActions();
	}

	private void readContractViolationActions() {
		for (JAXBElement<? extends ContractViolationAction> element : origin.getContractViolationAction()
				.getDefaultOrPackageOrClazz()) {
			readContractViolationAction(element);
		}
	}

	private void readContractViolationAction(JAXBElement<? extends ContractViolationAction> element) {
		if (element.getDeclaredType().equals(ContractViolationActionWithName.class)) {
			readContractViolationActionWithName((ContractViolationActionWithName) element.getValue());
		} else {
			readContractViolationAction(element.getValue());
		}
	}

	private void readContractViolationActionWithName(ContractViolationActionWithName value) {
		contractViolationActions.put(value.getName(), value);
	}

	private void readContractViolationAction(ContractViolationAction value) {
		defaultContractViolationAction = value;
	}

	public boolean writeTransformedClasses() {
		return origin.getWriteTransformedClasses().isValue();
	}

	public String getTransformedClassesOutputDir() {
		return origin.getWriteTransformedClasses().getDirectory();
	}

	public ContractViolationAction getContractViolationAction(String className) {
		return null;
	}
}
