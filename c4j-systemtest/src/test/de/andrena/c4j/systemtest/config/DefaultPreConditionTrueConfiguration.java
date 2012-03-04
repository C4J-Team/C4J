package de.andrena.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.c4j.DefaultConfiguration;

public class DefaultPreConditionTrueConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest.config.defaultpreconditiontrue");
	}

	@Override
	public DefaultPreCondition getDefaultPreCondition() {
		return DefaultPreCondition.TRUE;
	}
}
