package de.andrena.c4j;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultConfiguration implements Configuration {
	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to an empty set.
	 */
	@Override
	public Set<Configuration> getConfigurations() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to an empty set.
	 */
	@Override
	public Set<String> getRootPackages() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to an epty map.
	 */
	@Override
	public Map<Class<?>, Class<?>> getExternalContracts() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to an empty set.
	 */
	@Override
	public Set<Method> getPureWhitelist() throws NoSuchMethodException, SecurityException {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to false.
	 */
	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to {@link DefaultPreCondition#UNDEFINED}.
	 */
	@Override
	public DefaultPreCondition getDefaultPreCondition() {
		return DefaultPreCondition.UNDEFINED;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to {@link InvalidPreConditionBehavior#IGNORE_AND_WARN}.
	 */
	@Override
	public InvalidPreConditionBehavior getInvalidPreConditionBehavior() {
		return InvalidPreConditionBehavior.IGNORE_AND_WARN;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to a set including both {@link ContractViolationAction#LOG} and
	 *         {@link ContractViolationAction#ASSERTION_ERROR}.
	 */
	@Override
	public Set<ContractViolationAction> getContractViolationActions() {
		Set<ContractViolationAction> contractViolationActions = new HashSet<ContractViolationAction>();
		contractViolationActions.add(ContractViolationAction.LOG);
		contractViolationActions.add(ContractViolationAction.ASSERTION_ERROR);
		return contractViolationActions;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to a set including both {@link PureBehavior#SKIP_INVARIANTS} and
	 *         {@link PureBehavior#VALIDATE_PURE}.
	 */
	@Override
	public Set<PureBehavior> getPureBehaviors() {
		Set<PureBehavior> pureBehaviors = new HashSet<PureBehavior>();
		pureBehaviors.add(PureBehavior.SKIP_INVARIANTS);
		pureBehaviors.add(PureBehavior.VALIDATE_PURE);
		return pureBehaviors;
	}
}
