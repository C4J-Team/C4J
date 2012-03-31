package de.andrena.c4j.internal.evaluator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.util.ObjectMapper;
import de.andrena.c4j.internal.util.Pair;
import de.andrena.c4j.internal.util.SelfInitializingMap;

public class Evaluator {
	public static final StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static final StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static final StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");
	public static final StaticCall fieldAccess = new StaticCall(Evaluator.class, "fieldAccess");
	public static final StaticCall methodCall = new StaticCall(Evaluator.class, "methodCall");
	public static final StaticCall oldFieldAccess = new StaticCall(Evaluator.class, "oldFieldAccess");
	public static final StaticCall oldMethodCall = new StaticCall(Evaluator.class, "oldMethodCall");
	public static final StaticCall storeFieldAccess = new StaticCall(Evaluator.class, "storeFieldAccess");
	public static final StaticCall storeMethodCall = new StaticCall(Evaluator.class, "storeMethodCall");
	public static final StaticCall getCurrentTarget = new StaticCall(Evaluator.class, "getCurrentTarget");
	public static final StaticCall beforePre = new StaticCall(Evaluator.class, "beforePre");
	public static final StaticCall beforePost = new StaticCall(Evaluator.class, "beforePost");
	public static final StaticCall beforeInvariant = new StaticCall(Evaluator.class, "beforeInvariant");
	public static final StaticCall afterContract = new StaticCall(Evaluator.class, "afterContract");
	public static final StaticCall afterContractMethod = new StaticCall(Evaluator.class, "afterContractMethod");
	public static final StaticCall getContractFromCache = new StaticCall(Evaluator.class, "getContractFromCache");
	public static final StaticCall setException = new StaticCall(Evaluator.class, "setException");

	private static final Logger logger = Logger.getLogger(Evaluator.class);

	private static final ObjectMapper<Pair<Class<?>, Class<?>>, Object> contractCache = new ObjectMapper<Pair<Class<?>, Class<?>>, Object>();

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
	private final static ThreadLocal<Throwable> exceptionValue = new ThreadLocal<Throwable>();
	final static ThreadLocal<Object> currentTarget = new ThreadLocal<Object>();
	final static ThreadLocal<Class<?>> contractReturnType = new ThreadLocal<Class<?>>();

	/**
	 * Integer = stack trace depth, class = contract class
	 */
	static final ThreadLocal<Pair<Integer, Class<?>>> currentOldCacheEnvironment = new ThreadLocal<Pair<Integer, Class<?>>>();
	private static final ThreadLocal<SelfInitializingMap<Pair<Integer, Class<?>>, Map<String, Object>>> oldStore = new ThreadLocal<SelfInitializingMap<Pair<Integer, Class<?>>, Map<String, Object>>>() {
		@Override
		protected SelfInitializingMap<Pair<Integer, Class<?>>, Map<String, Object>> initialValue() {
			return new SelfInitializingMap<Pair<Integer, Class<?>>, Map<String, Object>>() {
				@Override
				protected Map<String, Object> initialValue() {
					return new HashMap<String, Object>();
				}

			};
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> T getCurrentTarget() {
		return (T) currentTarget.get();
	}

	public static int getOldStoreSize() {
		return oldStore.get().size();
	}

	public static boolean isBefore() {
		logger.trace("isBefore returning " + (evaluationPhase.get() == EvaluationPhase.BEFORE));
		return evaluationPhase.get() == EvaluationPhase.BEFORE;
	}

	public static boolean isAfter() {
		logger.trace("isAfter returning " + (evaluationPhase.get() == EvaluationPhase.AFTER));
		return evaluationPhase.get() == EvaluationPhase.AFTER;
	}

	public static Object oldFieldAccess(String fieldName) {
		Object oldValue = getCurrentOldCache().get(fieldName);
		logger.trace("oldFieldAccess for field '" + fieldName + "' with " + currentOldCacheEnvironment.get().getFirst()
				+ " " + currentOldCacheEnvironment.get().getSecond() + " returning " + oldValue);
		return oldValue;
	}

	private static Map<String, Object> getCurrentOldCache() {
		return oldStore.get().get(currentOldCacheEnvironment.get());
	}

	public static Object oldMethodCall(String methodName) {
		Object oldValue = getCurrentOldCache().get(methodName);
		logger.trace("oldMethodCall for method '" + methodName + "' with "
				+ currentOldCacheEnvironment.get().getFirst()
				+ " " + currentOldCacheEnvironment.get().getSecond() + " returning " + oldValue);
		return oldValue;
	}

	public static void storeFieldAccess(String fieldName) {
		Object storedValue = fieldAccess(fieldName);
		logger.trace("storeFieldAccess for field '" + fieldName + "' with "
				+ currentOldCacheEnvironment.get().getFirst() + " " + currentOldCacheEnvironment.get().getSecond()
				+ " storing " + storedValue);
		getCurrentOldCache().put(fieldName, storedValue);
	}

	public static void storeMethodCall(String methodName) {
		Object storedValue = methodCall(methodName, new Class<?>[0], new Object[0]);
		logger.trace("storeMethodCall for method '" + methodName + "' with "
				+ currentOldCacheEnvironment.get().getFirst() + " " + currentOldCacheEnvironment.get().getSecond()
				+ " storing " + storedValue);
		getCurrentOldCache().put(methodName, storedValue);
	}

	public static Object fieldAccess(String fieldName) {
		try {
			Object target = currentTarget.get();
			Field field = getInheritedField(fieldName, target.getClass());
			field.setAccessible(true);
			Object value = field.get(target);
			logger.trace("fieldAccess returning " + value);
			return value;
		} catch (Exception e) {
			throw new EvaluationException("could not access field " + fieldName, e);
		}
	}

	private static Field getInheritedField(String fieldName, Class<?> clazz) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() != null) {
				return getInheritedField(fieldName, clazz.getSuperclass());
			}
			throw e;
		}
	}

	public static Object methodCall(String methodName, Class<?>[] argTypes, Object[] args) {
		try {
			Object target = currentTarget.get();
			Method method = getInheritedMethod(methodName, target.getClass(), argTypes);
			method.setAccessible(true);
			Object value = method.invoke(target, args);
			logger.trace("methodCall returning " + value);
			return value;
		} catch (Exception e) {
			throw new EvaluationException("could not call method " + methodName, e);
		}
	}

	private static Method getInheritedMethod(String methodName, Class<? extends Object> clazz, Class<?>[] argTypes)
			throws NoSuchMethodException {
		try {
			return clazz.getDeclaredMethod(methodName, argTypes);
		} catch (NoSuchMethodException e) {
			if (clazz.getSuperclass() != null) {
				return getInheritedMethod(methodName, clazz.getSuperclass(), argTypes);
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReturnValue() {
		return (T) returnValue.get();
	}

	public static boolean beforePre(Object target, String methodName, Class<?> contractClass, Class<?> returnType) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.BEFORE);
			beforeContract(target, contractClass, returnType, new Exception().getStackTrace().length);
			logger.info("Calling pre-condition for " + methodName + " in contract " + contractClass.getSimpleName()
					+ ".");
			return true;
		}
		return false;
	}

	public static boolean beforeInvariant(Object target, String className, Class<?> contractClass) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.INVARIANT);
			beforeContract(target, contractClass, void.class, new Exception().getStackTrace().length);
			logger.info("Calling invariant for " + className + " in contract " + contractClass.getSimpleName() + ".");
			return true;
		}
		return false;
	}

	private static void beforeContract(Object target, Class<?> contractClass, Class<?> returnType, int stackTraceDepth) {
		currentTarget.set(target);
		currentOldCacheEnvironment.set(new Pair<Integer, Class<?>>(Integer.valueOf(stackTraceDepth), contractClass));
		contractReturnType.set(returnType);
	}

	public static boolean beforePost(Object target, String methodName, Class<?> contractClass, Class<?> returnType,
			Object actualReturnValue) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.AFTER);
			beforeContract(target, contractClass, returnType, new Exception().getStackTrace().length);
			returnValue.set(actualReturnValue);
			logger.info("Calling post-condition for " + methodName + " in contract " + contractClass.getSimpleName()
					+ ".");
			return true;
		}
		return false;
	}

	public static void afterContract() {
		contractReturnType.set(null);
		currentTarget.set(null);
		evaluationPhase.set(EvaluationPhase.NONE);
	}

	public static void afterContractMethod(Class<?> contractClass) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			logger.trace("afterContractMethod");
			returnValue.set(null);
			exceptionValue.set(null);
			oldStore.get()
					.get(new Pair<Integer, Class<?>>(Integer.valueOf(new Exception().getStackTrace().length),
							contractClass))
					.clear();
		}
	}

	public static Object getContractFromCache(Object target, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		Object contract;
		Pair<Class<?>, Class<?>> classPair = new Pair<Class<?>, Class<?>>(contractClass, callingClass);
		if (contractCache.contains(target, classPair)) {
			contract = contractCache.get(target, classPair);
		} else {
			contract = contractClass.newInstance();
			contractCache.put(target, classPair, contract);
		}
		return contract;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getConditionReturnValue() {
		if (!contractReturnType.get().isPrimitive()) {
			return null;
		}
		return (T) primitiveReturnValues.get(contractReturnType.get());
	}

	public static void setException(Throwable t) {
		exceptionValue.set(t);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T getException() {
		return (T) exceptionValue.get();
	}
}
