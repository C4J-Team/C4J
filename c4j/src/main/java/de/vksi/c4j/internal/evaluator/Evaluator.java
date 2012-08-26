package de.vksi.c4j.internal.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.util.ReflectionHelper;

public class Evaluator {
	public static final StaticCall isBefore = new StaticCall(Evaluator.class, "isBefore");
	public static final StaticCall isAfter = new StaticCall(Evaluator.class, "isAfter");
	public static final StaticCall getReturnValue = new StaticCall(Evaluator.class, "getReturnValue");
	public static final StaticCall getCurrentTarget = new StaticCall(Evaluator.class, "getCurrentTarget");
	public static final StaticCall getPreCondition = new StaticCall(Evaluator.class, "getPreCondition");
	public static final StaticCall getPostCondition = new StaticCall(Evaluator.class, "getPostCondition");
	public static final StaticCall getInvariant = new StaticCall(Evaluator.class, "getInvariant");
	public static final StaticCall canExecuteCondition = new StaticCall(Evaluator.class, "canExecuteCondition");
	public static final StaticCall canExecutePostCondition = new StaticCall(Evaluator.class, "canExecutePostCondition");
	public static final StaticCall beforeContractMethod = new StaticCall(Evaluator.class, "beforeContractMethod");
	public static final StaticCall afterContract = new StaticCall(Evaluator.class, "afterContract");
	public static final StaticCall afterContractMethod = new StaticCall(Evaluator.class, "afterContractMethod");
	public static final StaticCall setException = new StaticCall(Evaluator.class, "setException");

	private static final Logger logger = Logger.getLogger(Evaluator.class);
	private static final ReflectionHelper reflectionHelper = new ReflectionHelper();

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

	@SuppressWarnings("unchecked")
	public static <T> T getCurrentTarget() {
		return (T) currentTarget.get();
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

	@SuppressWarnings("unchecked")
	public static <T> T getReturnValue() {
		return (T) returnValue.get();
	}

	public static Object getPreCondition(Object target, String methodName, Class<?> contractClass,
			Class<?> callingClass, Class<?> returnType) throws InstantiationException, IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.BEFORE);
		beforeContract(target, returnType);
		logger.info("Calling pre-condition for " + methodName + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return ContractCache.getContractFromCache(target, contractClass, callingClass);
	}

	public static boolean canExecutePostCondition() {
		return evaluationPhase.get() == EvaluationPhase.NONE && !(exceptionValue.get() instanceof AssertionError);
	}

	public static boolean canExecuteCondition() {
		return evaluationPhase.get() == EvaluationPhase.NONE && exceptionValue.get() == null;
	}

	public static Object getInvariant(Object target, String className, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.INVARIANT);
		beforeContract(target, void.class);
		logger.info("Calling invariant for " + className + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return ContractCache.getContractFromCache(target, contractClass, callingClass);
	}

	private static void beforeContract(Object target, Class<?> returnType) {
		currentTarget.set(target);
		contractReturnType.set(returnType);
	}

	public static void beforeContractMethod() {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			OldCache.add();
			PureEvaluator.addUnchangeable();
			MaxTimeCache.add();
		}
	}

	public static Object getPostCondition(Object target, String methodName, Class<?> contractClass,
			Class<?> callingClass, Class<?> returnType, Object actualReturnValue) throws InstantiationException,
			IllegalAccessException {
		evaluationPhase.set(EvaluationPhase.AFTER);
		beforeContract(target, returnType);
		returnValue.set(actualReturnValue);
		logger.info("Calling post-condition for " + methodName + " in contract "
				+ reflectionHelper.getSimplerName(contractClass) + ".");
		return ContractCache.getContractFromCache(target, contractClass, callingClass);
	}

	public static void afterContract() {
		if (logger.isTraceEnabled()) {
			logger.trace("afterContract");
		}
		contractReturnType.set(null);
		currentTarget.set(null);
		evaluationPhase.set(EvaluationPhase.NONE);
		UnchangedCache.setClassInvariantConstructorCall(false);
	}

	public static void afterContractMethod() {
		if (evaluationPhase.get() == EvaluationPhase.NONE) {
			if (logger.isTraceEnabled()) {
				logger.trace("afterContractMethod");
			}
			returnValue.set(null);
			exceptionValue.set(null);
			OldCache.remove();
			PureEvaluator.removeUnchangeable();
			MaxTimeCache.remove();
		}
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
