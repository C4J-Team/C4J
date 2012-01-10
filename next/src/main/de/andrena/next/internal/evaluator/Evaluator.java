package de.andrena.next.internal.evaluator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.andrena.next.internal.compiler.StaticCall;
import de.andrena.next.internal.util.ObjectMapper;

public class Evaluator {
	public static final StaticCall before = new StaticCall(Evaluator.class, "before");
	public static final StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static final StaticCall after = new StaticCall(Evaluator.class, "after");
	public static final StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static final StaticCall callInvariant = new StaticCall(Evaluator.class, "callInvariant");
	public static final StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");
	public static final StaticCall fieldAccess = new StaticCall(Evaluator.class, "fieldAccess");
	public static final StaticCall methodCall = new StaticCall(Evaluator.class, "methodCall");
	public static final StaticCall oldFieldAccess = new StaticCall(Evaluator.class, "oldFieldAccess");
	public static final StaticCall oldMethodCall = new StaticCall(Evaluator.class, "oldMethodCall");
	public static final StaticCall storeFieldAccess = new StaticCall(Evaluator.class, "storeFieldAccess");
	public static final StaticCall storeMethodCall = new StaticCall(Evaluator.class, "storeMethodCall");

	private static final Logger logger = Logger.getLogger(Evaluator.class);

	private static final ObjectMapper<Class<?>, Object> contractCache = new ObjectMapper<Class<?>, Object>();

	private static final Map<Class<?>, Object> primitiveReturnValues = new HashMap<Class<?>, Object>() {
		private static final long serialVersionUID = 5365905181961089260L;
		{
			put(long.class, Long.valueOf(0));
			put(int.class, Integer.valueOf(0));
			put(short.class, Short.valueOf((short) 0));
			put(char.class, Character.valueOf((char) 0));
			put(byte.class, Byte.valueOf((byte) 0));
			put(double.class, Double.valueOf(0));
			put(float.class, Float.valueOf(0));
			put(boolean.class, Boolean.FALSE);
		}
	};

	static ThreadLocal<EvaluationPhase> evaluationPhase = new ThreadLocal<EvaluationPhase>() {
		@Override
		protected EvaluationPhase initialValue() {
			return EvaluationPhase.NONE;
		}
	};

	static enum EvaluationPhase {
		BEFORE, AFTER, NONE, INVARIANT;
	}

	final static ThreadLocal<Object> returnValue = new ThreadLocal<Object>();
	final static ThreadLocal<Object> currentTarget = new ThreadLocal<Object>();
	final static ThreadLocal<Class<?>> contractReturnType = new ThreadLocal<Class<?>>();

	private static final ThreadLocal<Map<String, Object>> oldStore = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> T getCurrentTarget() {
		return (T) currentTarget.get();
	}

	public static boolean isBefore() {
		logger.info("isBefore returning " + (Evaluator.evaluationPhase.get() == EvaluationPhase.BEFORE));
		return Evaluator.evaluationPhase.get() == EvaluationPhase.BEFORE;
	}

	public static boolean isAfter() {
		logger.info("isAfter returning " + (Evaluator.evaluationPhase.get() == EvaluationPhase.AFTER));
		return Evaluator.evaluationPhase.get() == EvaluationPhase.AFTER;
	}

	public static Object oldFieldAccess(String fieldName) {
		return oldStore.get().get(fieldName);
	}

	public static Object oldMethodCall(String methodName) {
		return oldStore.get().get(methodName);
	}

	public static void storeFieldAccess(String fieldName) {
		oldStore.get().put(fieldName, fieldAccess(fieldName));
	}

	public static void storeMethodCall(String methodName) {
		oldStore.get().put(methodName, methodCall(methodName, new Class<?>[0], new Object[0]));
	}

	public static Object fieldAccess(String fieldName) {
		try {
			Object target = Evaluator.currentTarget.get();
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			throw new EvaluationException("could not access field " + fieldName, e);
		}
	}

	public static Object methodCall(String methodName, Class<?>[] argTypes, Object[] args) {
		try {
			Object target = Evaluator.currentTarget.get();
			Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (Exception e) {
			throw new EvaluationException("could not call method " + methodName, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReturnValue() {
		return (T) Evaluator.returnValue.get();
	}

	public static void before(Object target, Class<?> contractClass, String methodName, Class<?>[] argTypes,
			Object[] args) {
		if (Evaluator.evaluationPhase.get() == EvaluationPhase.NONE) {
			Evaluator.evaluationPhase.set(EvaluationPhase.BEFORE);
			Evaluator.currentTarget.set(target);
			logger.info("before " + methodName);
			callContractMethod(contractClass, methodName, argTypes, args);
		}
	}

	public static void after(Object target, Class<?> contractClass, String methodName, Class<?>[] argTypes,
			Object[] args, Object returnValue) {
		if (Evaluator.evaluationPhase.get() == EvaluationPhase.NONE) {
			Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
			Evaluator.currentTarget.set(target);
			logger.info("setting return value to " + returnValue);
			Evaluator.returnValue.set(returnValue);
			logger.info("after " + methodName);
			callContractMethod(contractClass, methodName, argTypes, args);
		}
	}

	public static void callInvariant(Object target, Class<?> contractClass, String methodName) {
		if (Evaluator.evaluationPhase.get() == EvaluationPhase.NONE) {
			Evaluator.evaluationPhase.set(EvaluationPhase.INVARIANT);
			Evaluator.currentTarget.set(target);
			logger.info("calling invariant " + methodName);
			callContractMethod(contractClass, methodName, new Class<?>[0], new Object[0]);
		}
	}

	static void callContractMethod(Class<?> contractClass, String methodName, Class<?>[] argTypes, Object[] args)
			throws AssertionError {
		try {
			Object contract;
			if (contractCache.contains(Evaluator.currentTarget, contractClass)) {
				contract = contractCache.get(Evaluator.currentTarget, contractClass);
			} else {
				contract = contractClass.newInstance();
				contractCache.put(Evaluator.currentTarget, contractClass, contract);
			}
			Method method = contractClass.getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			logger.info("setting return type for " + method.getName() + " to " + method.getReturnType());
			contractReturnType.set(method.getReturnType());
			method.invoke(contract, args);
		} catch (InvocationTargetException e) {
			if (e.getTargetException().getClass().equals(AssertionError.class)) {
				throw (AssertionError) e.getTargetException();
			} else {
				throw new EvaluationException("exception while calling contract method " + methodName + " of class "
						+ contractClass.getName(), e.getTargetException());
			}
		} catch (Exception e) {
			throw new EvaluationException("could not call contract method " + methodName + " of class "
					+ contractClass.getName(), e);
		} finally {
			Evaluator.evaluationPhase.set(EvaluationPhase.NONE);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getConditionReturnValue() {
		if (!contractReturnType.get().isPrimitive()) {
			return null;
		}
		return (T) primitiveReturnValues.get(contractReturnType.get());
	}
}
