package de.andrena.c4j.internal.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.util.ObjectMapper;
import de.andrena.c4j.internal.util.Pair;
import de.andrena.c4j.internal.util.ReflectionHelper;
import de.andrena.c4j.internal.util.SelfInitializingMap;

public class Evaluator {
	public static final StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static final StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static final StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");
	public static final StaticCall oldRetrieve = new StaticCall(Evaluator.class, "oldRetrieve");
	public static final StaticCall oldStore = new StaticCall(Evaluator.class, "oldStore");
	public static final StaticCall getCurrentTarget = new StaticCall(Evaluator.class, "getCurrentTarget");
	public static final StaticCall getPreCondition = new StaticCall(Evaluator.class, "getPreCondition");
	public static final StaticCall getPostCondition = new StaticCall(Evaluator.class, "getPostCondition");
	public static final StaticCall getInvariant = new StaticCall(Evaluator.class, "getInvariant");
	public static final StaticCall canExecuteCondition = new StaticCall(Evaluator.class, "canExecuteCondition");
	public static final StaticCall afterContract = new StaticCall(Evaluator.class, "afterContract");
	public static final StaticCall afterContractMethod = new StaticCall(Evaluator.class, "afterContractMethod");
	public static final StaticCall setException = new StaticCall(Evaluator.class, "setException");
	public static final StaticCall isUnchanged = new StaticCall(Evaluator.class, "isUnchanged");

	private static final Logger logger = Logger.getLogger(Evaluator.class);
	private static final ReflectionHelper reflectionHelper = new ReflectionHelper();

	private static final ObjectMapper<Pair<Class<?>, Class<?>>, Object> contractCache = new ObjectMapper<Pair<Class<?>, Class<?>>, Object>();

	private static final Map<Class<?>, Object> primitiveReturnValues = new HashMap<Class<?>, Object>() {
		private static final long serialVersionUID = 5365905181961089260L;
		{
			put(boolean.class, Boolean.FALSE);
			put(byte.class, Byte.valueOf((byte) 0));
			put(char.class, Character.valueOf((char) 0));
			put(double.class, Double.valueOf(0));
			put(float.class, Float.valueOf(0));
			put(int.class, Integer.valueOf(0));
			put(long.class, Long.valueOf(0));
			put(short.class, Short.valueOf((short) 0));
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
	static final ThreadLocal<Integer> currentOldCacheEnvironment = new ThreadLocal<Integer>();
	private static final ThreadLocal<SelfInitializingMap<Integer, Map<Integer, Object>>> oldCache = new ThreadLocal<SelfInitializingMap<Integer, Map<Integer, Object>>>() {
		@Override
		protected SelfInitializingMap<Integer, Map<Integer, Object>> initialValue() {
			return new SelfInitializingMap<Integer, Map<Integer, Object>>() {
				@Override
				protected Map<Integer, Object> initialValue() {
					return new HashMap<Integer, Object>();
				}
			};
		}
	};

	private final static ThreadLocal<Object> unchangedCache = new ThreadLocal<Object>();

	public static boolean isUnchanged(Object compareObject, boolean triggerSetUnchangedCache) {
		// auto-boxing is evil, requires equals instead of ==
		if (compareObject instanceof Boolean || compareObject instanceof Byte || compareObject instanceof Character
				|| compareObject instanceof Double || compareObject instanceof Float
				|| compareObject instanceof Integer || compareObject instanceof Long || compareObject instanceof Short) {
			return compareObject.equals(unchangedCache.get());
		}
		PureEvaluator.unregisterUnchangeable(new Object[] { compareObject });
		return compareObject == unchangedCache.get();
	}

	public static void setUnchangedCache(Object value) {
		unchangedCache.set(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getCurrentTarget() {
		return (T) currentTarget.get();
	}

	public static int getOldStoreSize() {
		return oldCache.get().size();
	}

	public static boolean isBefore() {
		if (logger.isTraceEnabled()) {
			logger.trace("isBefore returning " + (evaluationPhase.get() == EvaluationPhase.BEFORE));
		}
		return evaluationPhase.get() == EvaluationPhase.BEFORE;
	}

	public static boolean isAfter() {
		if (logger.isTraceEnabled()) {
			logger.trace("isAfter returning " + (evaluationPhase.get() == EvaluationPhase.AFTER));
		}
		return evaluationPhase.get() == EvaluationPhase.AFTER;
	}

	public static Object oldRetrieve(int index) {
		Object oldValue = getCurrentOldCache().get(Integer.valueOf(index));
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + currentOldCacheEnvironment.get()
					+ " returning " + oldValue);
		}
		return oldValue;
	}

	private static Map<Integer, Object> getCurrentOldCache() {
		return oldCache.get().get(currentOldCacheEnvironment.get());
	}

	public static void oldStore(int index, Object value) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with "
					+ currentOldCacheEnvironment.get()
					+ " storing " + value);
		}
		getCurrentOldCache().put(index, value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReturnValue() {
		return (T) returnValue.get();
	}

	public static Object getPreCondition(Object target, String methodName, Class<?> contractClass,
			Class<?> callingClass, Class<?> returnType) throws InstantiationException, IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.BEFORE);
		beforeContract(target, returnType, Thread.currentThread().getStackTrace().length);
		logger.info("Calling pre-condition for " + methodName + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return getContractFromCache(target, contractClass, callingClass);
	}

	public static boolean canExecuteCondition() {
		return evaluationPhase.get() == EvaluationPhase.NONE && !(exceptionValue.get() instanceof AssertionError);
	}

	public static Object getInvariant(Object target, String className, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.INVARIANT);
		beforeContract(target, void.class, Thread.currentThread().getStackTrace().length);
		logger.info("Calling invariant for " + className + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return getContractFromCache(target, contractClass, callingClass);
	}

	private static void beforeContract(Object target, Class<?> returnType, int stackTraceDepth) {
		currentTarget.set(target);
		currentOldCacheEnvironment.set(Integer.valueOf(stackTraceDepth));
		contractReturnType.set(returnType);
	}

	public static Object getPostCondition(Object target, String methodName, Class<?> contractClass,
			Class<?> callingClass, Class<?> returnType, Object actualReturnValue) throws InstantiationException,
			IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.AFTER);
		beforeContract(target, returnType, Thread.currentThread().getStackTrace().length);
		returnValue.set(actualReturnValue);
		logger.info("Calling post-condition for " + methodName + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return getContractFromCache(target, contractClass, callingClass);
	}

	public static void afterContract() {
		if (logger.isTraceEnabled()) {
			logger.trace("afterContract");
		}
		contractReturnType.set(null);
		currentTarget.set(null);
		evaluationPhase.set(EvaluationPhase.NONE);
	}

	public static void afterContractMethod() {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			if (logger.isTraceEnabled()) {
				logger.trace("afterContractMethod");
			}
			returnValue.set(null);
			exceptionValue.set(null);
			oldCache.get()
					.get(Integer.valueOf(Thread.currentThread().getStackTrace().length))
					.clear();
		}
	}

	private static Object getContractFromCache(Object target, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		if (target == null) {
			return null;
		}
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
