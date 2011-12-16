package de.andrena.next;

import de.andrena.next.internal.Evaluator;

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

	public static <T> T old(T obj) {
		return obj;
	}

	public interface PreCondition {
	}

	public interface PostCondition {
	}

}
