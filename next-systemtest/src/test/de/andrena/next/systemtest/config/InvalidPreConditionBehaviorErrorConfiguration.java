package de.andrena.next.systemtest.config;

import java.util.Collections;
import java.util.Set;

import de.andrena.next.DefaultConfiguration;

public class InvalidPreConditionBehaviorErrorConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest.config.invalidpreconditionbehavior");
	}

	@Override
	public InvalidPreConditionBehavior getInvalidPreConditionBehavior() {
		return InvalidPreConditionBehavior.ABORT_AND_ERROR;
	}
}
