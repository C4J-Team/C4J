package de.andrena.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.c4j.DefaultConfiguration;

public class StrengtheningPreConditionAllowedConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest.config.strengtheningpreconditionallowed");
	}

	@Override
	public boolean isStrengtheningPreConditionAllowed() {
		return true;
	}
}
