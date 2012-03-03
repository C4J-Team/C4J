package de.andrena.next;

import java.lang.reflect.Method;
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
	 * Define external contracts, as an alternative to the @Contract annotation on the target class. Needed when the
	 * target class cannot be modified with the @Contract annotation.
	 * 
	 * @return A Map, mapping target classes to their corresponding contract class.
	 */
	Map<Class<?>, Class<?>> getExternalContracts();

	/**
	 * Define methods that are being assumed to be pure. Cannot contain methods from classes within one of the root
	 * packages.
	 */
	Set<Method> getPureWhitelist() throws NoSuchMethodException, SecurityException;

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
	 * The behavior when an invalid pre-condition is detected.
	 * 
	 * @see Configuration#getDefaultPreCondition
	 * @see InvalidPreConditionBehavior
	 */
	InvalidPreConditionBehavior getInvalidPreConditionBehavior();

	public enum InvalidPreConditionBehavior {
		IGNORE_AND_WARN, ABORT_AND_ERROR;
	}

	/**
	 * The actions taken on a contract violation.
	 * 
	 * @see ContractViolationAction
	 */
	Set<ContractViolationAction> getContractViolationActions();

	public enum ContractViolationAction {
		LOG, ASSERTION_ERROR;
	}

	/**
	 * Defines the behaviors of the @Pure annotation on methods or the @PureTarget annotation on contract methods.
	 * 
	 * @see PureBehavior
	 */
	Set<PureBehavior> getPureBehaviors();

	public enum PureBehavior {
		VALIDATE_PURE, SKIP_INVARIANTS;
	}
}
