package de.andrena.c4j.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.c4j.AbstractConfiguration;

public class PureBehaviorSkipOnlyConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest.config.purebehaviorskiponly");
	}

	@Override
	public Set<PureBehavior> getPureBehaviors() {
		return Collections.singleton(PureBehavior.SKIP_INVARIANTS);
	}
}
