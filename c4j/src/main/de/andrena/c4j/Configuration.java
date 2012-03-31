package de.andrena.c4j;

import java.util.Map;
import java.util.Set;

public interface Configuration {
	/**
	 * Define more configurations with different root packages.
	 */
	Set<Configuration> getConfigurations();

	/**
	 * Define the so-called root packages. Only classes within those packages will use this configuration.
	 * 
	 * @return Packages as Strings, e.g. "java.lang.util".
	 */
	Set<String> getRootPackages();

	/**
	 * Define external contracts, as an alternative to the @{@link ContractReference} annotation on the target class. Needed when
	 * the target class cannot be modified with the @{@link ContractReference} annotation.
	 * 
	 * @return A Map, mapping target classes (keys of the Map) to their corresponding contract class (values of the
	 *         Map).
	 */
	Map<Class<?>, Class<?>> getExternalContracts();

	/**
	 * Define methods that are being assumed to be pure or unpure outside of the specified root packages.
	 * 
	 * @return A {@link PureRegistry} with the specified pure and unpure methods.
	 */
	PureRegistry getPureRegistry() throws PureRegistryException;

	/**
	 * Whether transformed classes within root-packages are being written on disk after transformation.
	 */
	boolean writeTransformedClasses();

	/**
	 * The default pre-condition, if no pre-condition is explicitly defined. Note that only undefined pre-conditions may
	 * be strengthened by inheriting types.
	 * 
	 * @see DefaultPreCondition
	 */
	DefaultPreCondition getDefaultPreCondition();

	public enum DefaultPreCondition {
		TRUE, UNDEFINED;
	}

	/**
	 * If strengthening a pre-condition is allowed.
	 * 
	 * @see Configuration#getDefaultPreCondition
	 */
	boolean isStrengtheningPreConditionAllowed();

	/**
	 * The actions taken on a contract violation.
	 * 
	 * @see ContractViolationAction
	 */
	Set<ContractViolationAction> getContractViolationActions();

	public enum ContractViolationAction {
		/**
		 * Log contract violations using Log4J.
		 */
		LOG,
		/**
		 * Throw an assertion error on contract violation.
		 */
		ASSERTION_ERROR;
	}

	/**
	 * Defines the behaviors of the @{@link Pure} annotation on methods or the @{@link PureTarget} annotation on
	 * contract methods.
	 * 
	 * @see PureBehavior
	 */
	Set<PureBehavior> getPureBehaviors();

	public enum PureBehavior {
		/**
		 * Full validation, issueing contract violations on unpure access.
		 */
		VALIDATE_PURE,
		/**
		 * Skip class-invariants for pure methods.
		 */
		SKIP_INVARIANTS;
	}
}
