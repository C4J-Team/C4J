package de.vksi.c4j;

import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.MaxTimeCache;
import de.vksi.c4j.internal.evaluator.UnchangedCache;

/**
 * Allows the definition of pre- and post-conditions within contract-methods.
 */
public class Condition {
	/**
	 * Usable to define a pre-condition within a contract-method.
	 * 
	 * @return Whether the contract-method is being executed as a pre-condition.
	 */
	@Pure
	public static boolean preCondition() {
		return Evaluator.isBefore();
	}

	/**
	 * Usable to define a post-condition within a contract-method.
	 * 
	 * @return Whether the contract-method is being executed as a post-condition.
	 */
	@Pure
	public static boolean postCondition() {
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
	 * Usable within a post-condition or class-invariant to get the value (primitive types) or reference (Objects) of an
	 * expression at the beginning of the target method. May contain any expression except local variables.
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * &#064;{@link ContractReference}(ContractClass.class)
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
	 * @param expressionWithoutLocalVariables
	 *            An expression without local variables.
	 * @return The value (primitive types) or reference (Objects) to the expression at the beginning of the target
	 *         method.
	 */
	@Pure
	public static <T> T old(T expressionWithoutLocalVariables) {
		return expressionWithoutLocalVariables;
	}

	/**
	 * Usable within a class-invariant to skip certain assert-statements in constructors only, e.g. when refering to the
	 * old value of a field.
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * assert constructorCall() || target.intValue == old(target.intValue) + 1;
	 * </pre>
	 * 
	 * @return If the class-invariant is being called after a constructor.
	 */
	@Pure
	public static boolean constructorCall() {
		return UnchangedCache.isClassInvariantConstructorCall();
	}

	/**
	 * EXPERIMENTAL! Usable only if PureBehavior.VALIDATE_PURE is enabled for the class being validated.
	 * <p>
	 * Usable within a post-condition or class-invariant to ensure, that a value (primitive types) or state (Objects) of
	 * an expression remains unchanged.
	 * <p>
	 * Note: Will always return true whenever being called from a class-invariant after a constructor.
	 * 
	 * @param expressionWithoutLocalVariables
	 *            An expression without local variables.
	 * @return Whether the value (primitive types) or reference (Objects) has not been changed compared to the beginning
	 *         of the method.
	 */
	@Pure
	public static boolean unchanged(Object expressionWithoutLocalVariables) {
		UnchangedCache.setUnchangedCache(expressionWithoutLocalVariables);
		return false;
	}

	/**
	 * Usable within a post-condition or a class-invariant to ensure method execution within a specified duration.
	 * <p>
	 * Note: Accuracy not guaranteed. Internally, {@link System#nanoTime()} is used to determine method runtime. This
	 * mechanism will not interrupt method execution after the specified maximum time. It will only complain after the
	 * method has been successfully executed, no matter how long it takes.
	 * 
	 * @param seconds
	 *            The maximum time the method execution is allowed to last.
	 * @return Whether the specified maximum execution time was observed.
	 */
	@Pure
	public static boolean maxTime(double seconds) {
		return MaxTimeCache.isWithinMaxTime(seconds);
	}
}
