package next;

import next.customize.ConditionInfo;
import next.customize.ConditionProvider;
import next.internal.Evaluator;

import org.apache.log4j.Logger;

public class Condition implements ConditionProvider {
	private static Logger logger = Logger.getLogger(Condition.class);

	public static void pre(boolean condition) {
		logger.info("pre called");
		if (!condition) {
			throw new AssertionError();
		}
	}

	public static void post(boolean condition) {
		logger.info("post called");
		if (!condition) {
			throw new AssertionError();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T result(Class<T> returnType) {
		return (T) Evaluator.getReturnValue();
	}

	public static Object result() {
		return Evaluator.getReturnValue();
	}

	@Override
	public ConditionInfo getConditionInfo(String methodName) {
		// TODO Auto-generated method stub
		return null;
	}
}
