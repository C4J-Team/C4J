package de.andrena.next;

import de.andrena.next.internal.evaluator.Evaluator;

public class Condition {
	@Pure
	public static boolean pre() {
		return Evaluator.isBefore();
	}

	@Pure
	public static boolean post() {
		return Evaluator.isAfter();
	}

	@Pure
	public static <T> T result(Class<T> returnType) {
		return Evaluator.<T> getReturnValue();
	}

	@Pure
	public static <T> T result() {
		return Evaluator.<T> getReturnValue();
	}

	@Pure
	public static <T> T ignored() {
		return Evaluator.<T> getConditionReturnValue();
	}

	@Pure
	public static <T> T old(T fieldOrMethodWithoutParameters) {
		return fieldOrMethodWithoutParameters;
	}

	@Pure
	public static boolean unchanged(Object... fieldOrMethodWithoutParameters) {
		return false;
	}
}
