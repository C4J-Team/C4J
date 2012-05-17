package de.andrena.c4j;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface Configuration {
	/**
	 * Define more configurations with different root packages.
	 */
	Set<Configuration> getConfigurations();

	/**
	 * Define the so-called root packages. Only classes within those packages will use this configuration. Must contain
	 * at least one element for custom, user-defined configurations.
	 * 
	 * @return Packages as Strings, e.g. "java.lang.util".
	 */
	Set<String> getRootPackages();

	/**
	 * Define external contracts, as an alternative to the @{@link ContractReference} annotation on the target class or
	 * the @{@link Contract} annotation on the contract class.
	 * 
	 * @return A Map, mapping target classes (keys of the Map) to their corresponding contract class (values of the
	 *         Map).
	 */
	Map<Class<?>, Class<?>> getExternalContracts();

	/**
	 * Define external contracts, as an alternative to the @{@link ContractReference} annotation on the target class or
	 * the @{@link Contract} annotation on the contract class.
	 * 
	 * @return A Map, mapping target classes (keys of the Map) to their corresponding contract class (values of the
	 *         Map). The map contains Strings, which is useful if the bootstrap class-loader cannot yet locate the
	 *         mapped classes. If possible, {@link Configuration#getExternalContracts()} should be used as it is safe
	 *         when refactoring classes.
	 */
	Map<String, String> getExternalContractsAsStrings();

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
	 * This directory will be recursively searched for contract classes containing the @{@link Contract} annotation,
	 * making it possible to declare contracts non-intrusively in contrary to using @{@link ContractReference}.
	 * 
	 * @return A directory containing the contract classes to be searched for recursively, or null if no search should
	 *         be conducted. The directory can also be a JAR file.
	 */
	File getContractsDirectory();

	/**
	 * If strengthening a pre-condition is allowed.
	 * 
	 * @see Configuration#getDefaultPreCondition
	 */
	boolean isStrengtheningPreConditionAllowed();

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
		 * EXPERIMENTAL!
		 * <p>
		 * Full validation, issueing contract violations on unpure access.
		 */
		VALIDATE_PURE,
		/**
		 * Skip class-invariants for pure methods.
		 */
		SKIP_INVARIANTS;
	}
}
