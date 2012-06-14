package de.vksi.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;

public class LogOnlyConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.systemtest.config.logonly");
	}

	@Override
	public Set<ContractViolationAction> getContractViolationActions() {
		return Collections.singleton(ContractViolationAction.LOG);
	}
}
