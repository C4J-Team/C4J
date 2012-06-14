package de.vksi.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;

public class AssertionErrorOnlyConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.systemtest.config.assertionerroronly");
	}

	@Override
	public Set<ContractViolationAction> getContractViolationActions() {
		return Collections.singleton(ContractViolationAction.ASSERTION_ERROR);
	}
}
