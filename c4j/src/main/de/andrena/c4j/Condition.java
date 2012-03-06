package de.andrena.c4j;

import de.andrena.c4j.internal.evaluator.Evaluator;

/**
 * Provides a contract with methods, allowing the definition of pre- and post-conditions within contract-methods.
 */
public class Condition {
	/**
	 * Usable to define a pre-condition within a contract-method.
	 * 
	 * @return Whether the contract-method is being executed as a pre-condition.
	 */
	@Pure
	public static boolean pre() {
		return Evaluator.isBefore();
	}

	/**
	 * Usable to define a post-condition within a contract-method.
	 * 
	 * @return Whether the contract-method is being executed as a post-condition.
	 */
	@Pure
	public static boolean post() {
		return Evaluator.isAfter();
	}

	/**
	 * Usable within a post-condition to get the actual return value of a method.
	 * 
	 * @param returnType
	 *            The expected type of the returned value.
	 * @return The actual return value of the target method - null if the return type is void or an exception was
	 *         thrown.
	 */
	@Pure
	public static <T> T result(Class<T> returnType) {
		return Evaluator.<T> getReturnValue();
	}

	/**
	 * Usable within a post-condition to get the actual return value of a method.
	 * 
	 * @return The actual return value of the target method - null if the return type is void or an exception was
	 *         thrown.
	 */
	@Pure
	public static <T> T result() {
		return Evaluator.<T> getReturnValue();
	}

	/**
	 * Usable within a post-condition to get any uncaught exception of a method.
	 * 
	 * @return The uncaught exception of the target method - null if no exception was thrown.
	 */
	@Pure
	public static Throwable exception() {
		return Evaluator.getException();
	}

	/**
	 * Usable within a post-condition to see if any uncaught exception was thrown in a method.
	 * 
	 * @return Whether any uncaught exception was thrown in the target method.
	 */
	@Pure
	public static boolean exceptionThrown() {
		return exception() != null;
	}

	/**
	 * Usable within a post-condition to get an uncaught exception of the specified type of a method.
	 * 
	 * @param exceptionType
	 * @return The uncaught exception of the specified type of the target method - null if no exception of the specified
	 *         type or no exception at all was thrown.
	 */
	@Pure
	public static <T extends Throwable> T exceptionOfType(Class<T> exceptionType) {
		if (exception() != null && exceptionType.isInstance(exception())) {
			return Evaluator.<T> getException();
		}
		return null;
	}

	/**
	 * Usable within a post-condition to see if an uncaught exception of the specified type was thrown in a method.
	 * 
	 * @param exceptionType
	 *            The type of the exception being thrown.
	 * @return Whether an uncaught exception of the specified type was thrown in the target method. If an exception not
	 *         being an instance of exceptionType is thrown, it will return false.
	 */
	@Pure
	public static <T extends Throwable> boolean exceptionThrownOfType(Class<T> exceptionType) {
		return exceptionOfType(exceptionType) != null;
	}

	/**
	 * Convenience method - usable as the return type of contract-methods.
	 * 
	 * @return A compatible return value.
	 */
	@Pure
	public static <T> T ignored() {
		return Evaluator.<T> getConditionReturnValue();
	}

	/**
	 * Usable within a post-condition to get the value (primitive types) or reference (Objects) to a field or return
	 * value of a method without parameters at the beginning of the target method.
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * &#064;{@link Contract}(ContractClass.class)
	 * class TargetClass {
	 * 	protected int intValue;
	 * 	private String stringValue = &quot;sample&quot;;
	 * 
	 * 	&#064;{@link Pure}
	 * 	public String getStringValue() {
	 * 		return stringValue;
	 * 	}
	 * 
	 * 	public void increment() {
	 * 		intValue++;
	 * 		stringValue += intValue;
	 * 	}
	 * }
	 * 
	 * class ContractClass extends TargetClass {
	 * 	&#064;{@link Target}
	 * 	private TargetClass target;
	 * 
	 * 	&#064;Override
	 * 	public void increment() {
	 * 		if ({@link #post()}) {
	 * 			assert target.intValue == old(target.intValue) + 1;
	 * 			assert target.getStringValue().equals(old(target.stringValue) + 1);
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param fieldOrMethodWithoutParameters
	 *            The target field or method without parameters.
	 * @return The value (primitive types) or reference (Objects) to the field or method at the beginning of the target
	 *         method.
	 */
	@Pure
	public static <T> T old(T fieldOrMethodWithoutParameters) {
		return fieldOrMethodWithoutParameters;
	}

	/**
	 * Usable within a post-condition to ensure, that a value (primitive types) or state (Objects) of a parameter, field
	 * or return value of a method without parameters remains unchanged.
	 * <p>
	 * Note that reassignment of parameters is allowed, as it's only visible within the method. This also means that
	 * parameters of primitive types are not affected.
	 * 
	 * @param parameterOrFieldOrMethodWithoutParameters
	 *            The method parameter, target field or method without parameters.
	 * @return Whether the value (primitive types) or reference (Objects) has not been changed compared to the beginning
	 *         of the method.
	 */
	@Pure
	public static boolean unchanged(Object... parameterOrFieldOrMethodWithoutParameters) {
		return false;
	}
}
