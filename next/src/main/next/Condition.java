package next;

import next.customize.ConditionInfo;
import next.customize.ConditionProvider;
import next.internal.Evaluator;

import org.apache.log4j.Logger;

public class Condition implements ConditionProvider {
	private static Logger logger = Logger.getLogger(Condition.class);

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

	@Override
	public ConditionInfo getConditionInfo(String methodName) {
		// TODO Auto-generated method stub
		return null;
	}
}
