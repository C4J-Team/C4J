package de.vksi.c4j.internal.evaluator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.util.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
	public static final StaticCall registerUnchangeable = new StaticCall(PureEvaluator.class, "registerUnchangeable");
	public static final StaticCall unregisterUnpure = new StaticCall(PureEvaluator.class, "unregisterUnpure");
	public static final StaticCall checkUnpureAccess = new StaticCall(PureEvaluator.class, "checkUnpureAccess");
	public static final StaticCall checkExternalAccess = new StaticCall(PureEvaluator.class, "checkExternalAccess");
	public static final StaticCall checkExternalBlacklistAccess = new StaticCall(PureEvaluator.class,
			"checkExternalBlacklistAccess");
	public static final StaticCall checkUnpureStatic = new StaticCall(PureEvaluator.class, "checkUnpureStatic");

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
		if (cacheContains(target)) {
			AssertionError assertionError = new AssertionError("illegal access on unpure method or field");
			logger.error(assertionError.getMessage(), assertionError);
			throw assertionError;
		}
	}

	private static boolean cacheContains(Object target) {
		return cacheContains(unpureCache, target) || cacheContains(unchangeableCache, target);

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
		if (cacheContains(target)) {
			warnExternalAccess(method);
		}
	}

	public static void warnExternalAccess(String method) {
		logger.warn("Access on unknown method " + method
				+ " outside the root-packages. Add it to the pure-registry in the configuration.");
	}

	public static void checkExternalBlacklistAccess(Object target, String method) {
		if (cacheContains(target)) {
			AssertionError assertionError = new AssertionError("illegal access on unpure method " + method
					+ " outside the root-packages.");
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
