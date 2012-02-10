package de.andrena.next.internal.evaluator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.andrena.next.internal.compiler.StaticCall;
import de.andrena.next.internal.util.ObjectMapper;
import de.andrena.next.internal.util.Pair;
import de.andrena.next.internal.util.SelfInitializingMap;

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
	final static ThreadLocal<Object> currentTarget = new ThreadLocal<Object>();
	final static ThreadLocal<Class<?>> contractReturnType = new ThreadLocal<Class<?>>();

	/**
	 * Integer = stack trace depth, class = contract class
	 */
	private static final ThreadLocal<Pair<Integer, Class<?>>> currentOldCacheEnvironment = new ThreadLocal<Pair<Integer, Class<?>>>();
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
		logger.info("isBefore returning " + (evaluationPhase.get() == EvaluationPhase.BEFORE));
		return evaluationPhase.get() == EvaluationPhase.BEFORE;
	}

	public static boolean isAfter() {
		logger.info("isAfter returning " + (evaluationPhase.get() == EvaluationPhase.AFTER));
		return evaluationPhase.get() == EvaluationPhase.AFTER;
	}

	public static Object oldFieldAccess(String fieldName) {
		logger.info("oldFieldAccess for field '" + fieldName + "' with " + currentOldCacheEnvironment.get().getFirst()
				+ " " + currentOldCacheEnvironment.get().getSecond());
		return getCurrentOldCache().get(fieldName);
	}

	private static Map<String, Object> getCurrentOldCache() {
		return oldStore.get().get(currentOldCacheEnvironment.get());
	}

	public static Object oldMethodCall(String methodName) {
		logger.info("oldMethodCall for method '" + methodName + "' with " + currentOldCacheEnvironment.get().getFirst()
				+ " " + currentOldCacheEnvironment.get().getSecond());
		return getCurrentOldCache().get(methodName);
	}

	public static void storeFieldAccess(String fieldName) {
		logger.info("storeFieldAccess for field '" + fieldName + "' with "
				+ currentOldCacheEnvironment.get().getFirst() + " " + currentOldCacheEnvironment.get().getSecond());
		getCurrentOldCache().put(fieldName, fieldAccess(fieldName));
	}

	public static void storeMethodCall(String methodName) {
		logger.info("storeMethodCall for method '" + methodName + "' with "
				+ currentOldCacheEnvironment.get().getFirst() + " " + currentOldCacheEnvironment.get().getSecond());
		getCurrentOldCache().put(methodName, methodCall(methodName, new Class<?>[0], new Object[0]));
	}

	public static Object fieldAccess(String fieldName) {
		try {
			Object target = currentTarget.get();
			Field field = getInheritedField(fieldName, target.getClass());
			field.setAccessible(true);
			return field.get(target);
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
			Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (Exception e) {
			throw new EvaluationException("could not call method " + methodName, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReturnValue() {
		return (T) returnValue.get();
	}

	public static boolean beforePre(Object target, Class<?> contractClass, Class<?> returnType) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.BEFORE);
			beforeContract(target, contractClass, returnType, new Exception().getStackTrace().length);
			return true;
		}
		return false;
	}

	public static boolean beforeInvariant(Object target, Class<?> contractClass) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.INVARIANT);
			beforeContract(target, contractClass, void.class, new Exception().getStackTrace().length);
			return true;
		}
		return false;
	}

	private static void beforeContract(Object target, Class<?> contractClass, Class<?> returnType, int stackTraceDepth) {
		currentTarget.set(target);
		currentOldCacheEnvironment.set(new Pair<Integer, Class<?>>(Integer.valueOf(stackTraceDepth), contractClass));
		contractReturnType.set(returnType);
	}

	public static boolean beforePost(Object target, Class<?> contractClass, Class<?> returnType,
			Object actualReturnValue) {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			evaluationPhase.set(EvaluationPhase.AFTER);
			beforeContract(target, contractClass, returnType, new Exception().getStackTrace().length);
			returnValue.set(actualReturnValue);
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
		logger.info("afterContractMethod");
		returnValue.set(null);
		oldStore.get()
				.get(new Pair<Integer, Class<?>>(Integer.valueOf(new Exception().getStackTrace().length), contractClass))
				.clear();
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
}
