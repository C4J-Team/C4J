package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;

public class DefaultPreConditionTrueConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest.config.defaultpreconditiontrue");
	}

	@Override
	public DefaultPreCondition getDefaultPreCondition() {
		return DefaultPreCondition.TRUE;
	}
}
