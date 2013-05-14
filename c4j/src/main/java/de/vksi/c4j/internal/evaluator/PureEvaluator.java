package de.vksi.c4j.internal.evaluator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.types.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
	public static final StaticCall registerUnchangeable = new StaticCall(PureEvaluator.class, "registerUnchangeable");
	public static final String registerUnchangeableDescriptor = "(Ljava/lang/Object;)V";
	public static final StaticCall unregisterUnpure = new StaticCall(PureEvaluator.class, "unregisterUnpure");
	public static final StaticCall checkUnpureAccess = new StaticCall(PureEvaluator.class, "checkUnpureAccess");
	public static final StaticCall checkExternalAccess = new StaticCall(PureEvaluator.class, "checkExternalAccess");
	public static final StaticCall checkExternalBlacklistAccess = new StaticCall(PureEvaluator.class,
			"checkExternalBlacklistAccess");
	public static final StaticCall checkUnpureStatic = new StaticCall(PureEvaluator.class, "checkUnpureStatic");

	public static enum ErrorType {
		UNPURE("unpure"), UNCHANGEABLE("unchangeable"), NONE(null);

		private String message;

		private ErrorType(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return message;
		}
	}

	private static final ThreadLocal<Deque<ObjectIdentitySet>> unpureCache = new ThreadLocal<Deque<ObjectIdentitySet>>() {
		@Override
		protected Deque<ObjectIdentitySet> initialValue() {
			return new ArrayDeque<ObjectIdentitySet>();
		}
	};
	private static final ThreadLocal<Deque<ObjectIdentitySet>> unchangeableCache = new ThreadLocal<Deque<ObjectIdentitySet>>() {
		@Override
		protected Deque<ObjectIdentitySet> initialValue() {
			return new ArrayDeque<ObjectIdentitySet>();
		}
	};

	private static ThreadLocal<Integer> pureCallDepth = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return Integer.valueOf(0);
		}
	};
	private static Logger logger = Logger.getLogger(PureEvaluator.class);

	public static boolean isUnpureCacheEmpty() {
		for (ObjectIdentitySet element : unpureCache.get()) {
			if (!element.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static void registerUnpure(Object[] objects) {
		unpureCache.get().addFirst(new ObjectIdentitySet());
		pureCallDepth.set(Integer.valueOf(pureCallDepth.get().intValue() + 1));
		addToCache(unpureCache, objects);
	}

	public static void registerUnchangeable(Object object) {
		if (object instanceof Boolean || object instanceof Byte || object instanceof Character
				|| object instanceof Double || object instanceof Float || object instanceof Integer
				|| object instanceof Long || object instanceof Short) {
			return;
		}
		addToCache(unchangeableCache, new Object[] { object });
	}

	private static void addToCache(ThreadLocal<Deque<ObjectIdentitySet>> cache, Object[] objects) {
		cache.get().peekFirst().addAll(Arrays.asList(objects));
		for (Object obj : objects) {
			if (obj instanceof Object[]) {
				addToCache(cache, (Object[]) obj);
			}
		}
	}

	public static void unregisterUnpure() {
		unpureCache.get().removeFirst();
		pureCallDepth.set(Integer.valueOf(pureCallDepth.get().intValue() - 1));
	}

	public static void checkUnpureAccess(Object target) {
		ErrorType errorType = getAccessErrorType(target);
		if (errorType != ErrorType.NONE) {
			AssertionError assertionError = new AssertionError("illegal access on " + errorType + " object");
			logger.error(assertionError.getMessage(), assertionError);
			throw assertionError;
		}
	}

	private static ErrorType getAccessErrorType(Object target) {
		if (cacheContains(unpureCache, target)) {
			return ErrorType.UNPURE;
		}
		if (cacheContains(unchangeableCache, target)) {
			return ErrorType.UNCHANGEABLE;
		}
		return ErrorType.NONE;
	}

	private static boolean cacheContains(ThreadLocal<Deque<ObjectIdentitySet>> cache, Object target) {
		for (ObjectIdentitySet element : cache.get()) {
			if (element.contains(target)) {
				return true;
			}
		}
		return false;
	}

	public static void checkExternalAccess(Object target, String method) {
		ErrorType errorType = getAccessErrorType(target);
		if (errorType != ErrorType.NONE) {
			warnExternalAccess(method, errorType);
		}
	}

	public static void warnExternalAccess(String method, ErrorType errorType) {
		logger.warn("Access on " + errorType + " object, method " + method
				+ " outside the root-packages. Add it to the pure-registry in the configuration.");
	}

	public static void checkExternalBlacklistAccess(Object target, String method) {
		ErrorType errorType = getAccessErrorType(target);
		if (errorType != ErrorType.NONE) {
			AssertionError assertionError = new AssertionError("illegal access on " + errorType + " object, method "
					+ method + " outside the root-packages.");
			logger.error(assertionError.getMessage(), assertionError);
			throw assertionError;
		}
	}

	public static void checkUnpureStatic() {
		if (pureCallDepth.get().intValue() > 0) {
			AssertionError assertionError = new AssertionError("illegal method access on unpure static method or field");
			logger.error(assertionError.getMessage(), assertionError);
			throw assertionError;
		}
	}

	public static void addUnchangeable() {
		unchangeableCache.get().addFirst(new ObjectIdentitySet());
	}

	public static void removeUnchangeable() {
		unchangeableCache.get().removeFirst();
	}
}
