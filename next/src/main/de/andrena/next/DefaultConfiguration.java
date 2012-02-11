package de.andrena.next;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class DefaultConfiguration implements Configuration {
	@Override
	public Set<Configuration> getConfigurations() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getRootPackages() {
		return Collections.emptySet();
	}

	@Override
	public Set<Method> getPureWhitelist() {
		return Collections.emptySet();
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

	@Override
	public DefaultPreCondition getDefaultPreCondition() {
		return DefaultPreCondition.UNDEFINED;
	}

	@Override
	public InvalidPreConditionBehavior getInvalidPreConditionBehavior() {
		return InvalidPreConditionBehavior.IGNORE_AND_WARN;
	}
}
