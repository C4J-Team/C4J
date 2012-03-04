package de.andrena.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.c4j.DefaultConfiguration;

public class PureBehaviorEmptyConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest.config.purebehaviorempty");
	}

	@Override
	public Set<PureBehavior> getPureBehaviors() {
		return Collections.emptySet();
	}
}
