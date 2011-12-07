package next;

import next.internal.Evaluator;

import org.apache.log4j.Logger;

public class Condition {
	private static Logger logger = Logger.getLogger(Condition.class);

	public static void pre() {
	}

	public static void post() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T result(Class<T> returnType) {
		return (T) Evaluator.getReturnValue();
	}

	public static Object result() {
		return Evaluator.getReturnValue();
	}

	public static <T> T ignored() {
		return Evaluator.getConditionReturnValue();
	}

	public static <T> T old(T obj) {
		return obj;
	}

}
