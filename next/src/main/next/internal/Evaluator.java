package next.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import next.internal.compiler.StaticCall;

import org.apache.log4j.Logger;

public class Evaluator {
	public static StaticCall before = new StaticCall(Evaluator.class, "before");
	public static StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static StaticCall after = new StaticCall(Evaluator.class, "after");
	public static StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");
	public static StaticCall fieldAccess = new StaticCall(Evaluator.class, "fieldAccess");
	public static StaticCall methodCall = new StaticCall(Evaluator.class, "methodCall");

	private static Logger logger = Logger.getLogger(Evaluator.class);

	private static ThreadLocal<EvaluationPhase> evaluationPhase = new ThreadLocal<EvaluationPhase>() {
		@Override
		protected EvaluationPhase initialValue() {
			return EvaluationPhase.BEFORE;
		}
	};

	private static ThreadLocal<Object> returnValue = new ThreadLocal<Object>();
	private static ThreadLocal<Object> currentTarget = new ThreadLocal<Object>();

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

	public static Object fieldAccess(String fieldName) {
		try {
			Object target = Evaluator.currentTarget.get();
			System.out.println(target.getClass());
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object methodCall(String methodName, Class<?>[] argTypes, Object[] args) {
		try {
			Object target = Evaluator.currentTarget.get();
			Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object getReturnValue() {
		return Evaluator.returnValue.get();
	}

	public static void before(Object target, Class<?> contractClass, String methodName, Class<?>[] argTypes,
			Object[] args) {
		Evaluator.evaluationPhase.set(EvaluationPhase.BEFORE);
		Evaluator.currentTarget.set(target);
		logger.info("before " + methodName);
		callContractMethod(contractClass, methodName, argTypes, args);
	}

	public static void after(Object target, Class<?> contractClass, String methodName, Class<?>[] argTypes,
			Object[] args, Object returnValue) {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		Evaluator.currentTarget.set(target);
		logger.info("setting return value to " + returnValue);
		Evaluator.returnValue.set(returnValue);
		logger.info("after " + methodName);
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
}
