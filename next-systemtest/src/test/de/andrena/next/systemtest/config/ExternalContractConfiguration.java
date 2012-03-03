package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;
import de.andrena.next.systemtest.config.externalcontract.ExternalContractSystemTest;

public class ExternalContractConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		new ExternalContractSystemTest.TargetClass();
		return Collections.singleton("de.andrena.next.systemtest.config.externalcontract");
	}

	@Override
	public Map<Class<?>, Class<?>> getExternalContracts() {
		Map<Class<?>, Class<?>> externalContracts = new HashMap<Class<?>, Class<?>>();
		externalContracts.put(ExternalContractSystemTest.TargetClass.class,
				ExternalContractSystemTest.ContractClass.class);
		externalContracts.put(ExternalContractSystemTest.TargetClassWithLocalAndExternalContract.class,
				ExternalContractSystemTest.ExternalContract.class);
		return externalContracts;
	}

}
