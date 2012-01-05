package de.andrena.next;

import de.andrena.next.internal.evaluator.Evaluator;

public class Condition {
	public static boolean pre() {
		return Evaluator.isBefore();
	}

	public static boolean post() {
		return Evaluator.isAfter();
	}

	@SuppressWarnings("unchecked")
	public static <T> T result(Class<T> returnType) {
		return (T) Evaluator.getReturnValue();
	}

	public static Object result() {
		return Evaluator.getReturnValue();
	}

	public static <T> T ignored() {
		return Evaluator.<T> getConditionReturnValue();
	}

	public static <T> T old(T fieldOrMethodWithoutParameters) {
		return fieldOrMethodWithoutParameters;
	}

	public static void unchanged(Object... fieldOrMethodWithoutParameters) {
	}
}
