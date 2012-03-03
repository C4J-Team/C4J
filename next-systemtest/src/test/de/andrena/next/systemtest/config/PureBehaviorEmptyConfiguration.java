package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;

public class PureBehaviorEmptyConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest.config.purebehaviorempty");
	}

	@Override
	public Set<PureBehavior> getPureBehaviors() {
		return Collections.emptySet();
	}
}
