package de.andrena.next;

import de.andrena.next.internal.evaluator.Evaluator;

public class Condition {
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

	public interface PreCondition {
	}

	public interface PostCondition {
	}

}
