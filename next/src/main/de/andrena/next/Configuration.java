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

	/**
	 * The behavior when an invalid pre-condition is detected.
	 * 
	 * @see Configuration#getDefaultPreCondition
	 * @see InvalidPreConditionBehavior
	 */
	InvalidPreConditionBehavior getInvalidPreConditionBehavior();

	Set<ContractViolationAction> getContractViolationActions();

	public enum DefaultPreCondition {
		TRUE, UNDEFINED;
	}

	public enum InvalidPreConditionBehavior {
		IGNORE_AND_WARN, ABORT_AND_ERROR;
	}

	public enum ContractViolationAction {
		LOG, ASSERTION_ERROR;
	}
}
