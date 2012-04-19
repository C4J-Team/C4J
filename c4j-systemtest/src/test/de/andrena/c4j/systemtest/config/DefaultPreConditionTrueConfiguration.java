package de.andrena.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.c4j.AbstractConfiguration;

public class DefaultPreConditionTrueConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest.config.defaultpreconditiontrue");
	}

	@Override
	public DefaultPreCondition getDefaultPreCondition() {
		return DefaultPreCondition.TRUE;
	}
}
