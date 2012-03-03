package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;

public class PureBehaviorSkipOnlyConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest.config.purebehaviorskiponly");
	}

	@Override
	public Set<PureBehavior> getPureBehaviors() {
		return Collections.singleton(PureBehavior.SKIP_INVARIANTS);
	}
}
