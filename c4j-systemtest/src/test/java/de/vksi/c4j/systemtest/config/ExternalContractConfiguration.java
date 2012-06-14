package de.vksi.c4j.systemtest.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;
import de.vksi.c4j.systemtest.config.externalcontract.ExternalContractSystemTest;
import de.vksi.c4j.systemtest.config.externalcontract.ObjectContractSystemTest.ObjectContract;

public class ExternalContractConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		new ExternalContractSystemTest.TargetClass();
		return Collections.singleton("de.vksi.c4j.systemtest.config.externalcontract");
	}

	@Override
	public Map<Class<?>, Class<?>> getExternalContracts() {
		Map<Class<?>, Class<?>> externalContracts = new HashMap<Class<?>, Class<?>>();
		externalContracts.put(ExternalContractSystemTest.TargetClass.class,
				ExternalContractSystemTest.ContractClass.class);
		externalContracts.put(ExternalContractSystemTest.TargetClassWithLocalAndExternalContract.class,
				ExternalContractSystemTest.ExternalContract.class);
		externalContracts.put(Object.class, ObjectContract.class);
		return externalContracts;
	}
}
