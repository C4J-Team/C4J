package de.andrena.next;

import de.andrena.next.internal.evaluator.Evaluator;

public class Condition {
	public static boolean pre() {
		return Evaluator.isBefore();
	}

	public static boolean post() {
		return Evaluator.isAfter();
	}

	public static <T> T result(Class<T> returnType) {
		return Evaluator.<T> getReturnValue();
	}

	public static <T> T result() {
		return Evaluator.<T> getReturnValue();
	}

	public static <T> T ignored() {
		return Evaluator.<T> getConditionReturnValue();
	}

	public static <T> T old(T fieldOrMethodWithoutParameters) {
		return fieldOrMethodWithoutParameters;
	}

	public static boolean unchanged(Object... fieldOrMethodWithoutParameters) {
		return false;
	}

	public static <T> T target(Class<T> targetType) {
		return Evaluator.<T> getCurrentTarget();
	}

	public static <T> T target() {
		return Evaluator.<T> getCurrentTarget();
	}
}
