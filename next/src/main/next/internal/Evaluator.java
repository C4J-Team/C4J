package next.internal;

import java.lang.reflect.InvocationTargetException;

import next.internal.compiler.StaticCall;

import org.apache.log4j.Logger;

public class Evaluator {
	public static StaticCall before = new StaticCall(Evaluator.class, "before");
	public static StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static StaticCall after = new StaticCall(Evaluator.class, "after");
	public static StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");

	private static Logger logger = Logger.getLogger(Evaluator.class);

	private static ThreadLocal<EvaluationPhase> evaluationPhase = new ThreadLocal<EvaluationPhase>() {
		@Override
		protected EvaluationPhase initialValue() {
			return EvaluationPhase.BEFORE;
		}
	};

	private static ThreadLocal<Object> returnValue = new ThreadLocal<Object>() {
		@Override
		protected Object initialValue() {
			return null;
		}
	};

	private static enum EvaluationPhase {
		BEFORE, AFTER;
	}

	public static boolean isBefore() {
		logger.info("isBefore returning " + (Evaluator.evaluationPhase.get() == EvaluationPhase.BEFORE));
		return Evaluator.evaluationPhase.get() == EvaluationPhase.BEFORE;
	}

	public static boolean isAfter() {
		logger.info("isAfter returning " + (Evaluator.evaluationPhase.get() == EvaluationPhase.AFTER));
		return Evaluator.evaluationPhase.get() == EvaluationPhase.AFTER;
	}

	public static Object getReturnValue() {
		return Evaluator.returnValue.get();
	}

	public static void before(Class<?> contractClass, String methodName, Class<?>[] argTypes, Object[] args) {
		Evaluator.evaluationPhase.set(EvaluationPhase.BEFORE);
		logger.info("before " + methodName);
		callContractMethod(contractClass, methodName, argTypes, args);
	}

	private static void callContractMethod(Class<?> contractClass, String methodName, Class<?>[] argTypes, Object[] args)
			throws AssertionError {
		try {
			Object contract = contractClass.newInstance();
			contractClass.getMethod(methodName, argTypes).invoke(contract, args);
		} catch (InvocationTargetException e) {
			if (e.getTargetException().getClass().equals(AssertionError.class)) {
				throw (AssertionError) e.getTargetException();
			} else {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void after(Class<?> contractClass, String methodName, Class<?>[] argTypes, Object[] args,
			Object returnValue) {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		logger.info("setting return value to " + returnValue);
		Evaluator.returnValue.set(returnValue);
		logger.info("after " + methodName);
		callContractMethod(contractClass, methodName, argTypes, args);
	}
}
