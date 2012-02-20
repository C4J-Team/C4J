package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;

public class LogOnlyConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest.config.logonly");
	}

	@Override
	public Set<ContractViolationAction> getContractViolationActions() {
		return Collections.singleton(ContractViolationAction.LOG);
	}
}
