package de.andrena.c4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractConfiguration implements Configuration {
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
	 * @return Defaults to an empty map.
	 */
	@Override
	public Map<Class<?>, Class<?>> getExternalContracts() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Defaults to an empty registry.
	 */
	@Override
	public PureRegistry getPureRegistry() throws PureRegistryException {
		return PureRegistry.union();
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
	 * @return Defaults to false.
	 */
	@Override
	public boolean isStrengtheningPreConditionAllowed() {
		return false;
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
	 * @return Defaults to a set including only {@link PureBehavior#SKIP_INVARIANTS}.
	 */
	@Override
	public Set<PureBehavior> getPureBehaviors() {
		return Collections.singleton(PureBehavior.SKIP_INVARIANTS);
	}

}