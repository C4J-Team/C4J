package de.andrena.next;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
	public Map<Class<?>, Class<?>> getExternalContracts() {
		return Collections.emptyMap();
	}

	@Override
	public Set<Method> getPureWhitelist() throws NoSuchMethodException, SecurityException {
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

	@Override
	public Set<ContractViolationAction> getContractViolationActions() {
		Set<ContractViolationAction> contractViolationActions = new HashSet<ContractViolationAction>();
		contractViolationActions.add(ContractViolationAction.LOG);
		contractViolationActions.add(ContractViolationAction.ASSERTION_ERROR);
		return contractViolationActions;
	}
}
