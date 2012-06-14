package de.vksi.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;

public class StrengtheningPreConditionAllowedConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.systemtest.config.strengtheningpreconditionallowed");
	}

	@Override
	public boolean isStrengtheningPreConditionAllowed() {
		return true;
	}
}
