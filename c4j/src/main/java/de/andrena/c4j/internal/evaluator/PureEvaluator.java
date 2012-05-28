package de.andrena.c4j.internal.evaluator;

import java.util.Arrays;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.util.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
	public static final StaticCall registerUnchangeable = new StaticCall(PureEvaluator.class, "registerUnchangeable");
	public static final StaticCall unregisterUnpure = new StaticCall(PureEvaluator.class, "unregisterUnpure");
	public static final StaticCall checkUnpureAccess = new StaticCall(PureEvaluator.class, "checkUnpureAccess");
	public static final StaticCall checkExternalAccess = new StaticCall(PureEvaluator.class, "checkExternalAccess");
	public static final StaticCall checkExternalBlacklistAccess = new StaticCall(PureEvaluator.class,
			"checkExternalBlacklistAccess");
	public static final StaticCall checkUnpureStatic = new StaticCall(PureEvaluator.class, "checkUnpureStatic");

	private static ThreadLocal<ObjectIdentitySet> unpureCache = new ThreadLocal<ObjectIdentitySet>() {
		@Override
		protected ObjectIdentitySet initialValue() {
			return new ObjectIdentitySet();
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
		return unpureCache.get().isEmpty();
	}

	public static void registerUnpure(Object[] objects) {
		pureCallDepth.set(Integer.valueOf(pureCallDepth.get().intValue() + 1));
		addToUnpureCache(objects);
	}

	public static void registerUnchangeable(Object object) {
		if (object instanceof Boolean || object instanceof Byte || object instanceof Character
				|| object instanceof Double || object instanceof Float
				|| object instanceof Integer || object instanceof Long || object instanceof Short) {
			return;
		}
		addToUnpureCache(new Object[] { object });
	}

	private static void addToUnpureCache(Object[] objects) {
		unpureCache.get().addAll(Arrays.asList(objects));
		for (Object obj : objects) {
			if (obj instanceof Object[]) {
				addToUnpureCache((Object[]) obj);
			}
		}
	}

	public static void unregisterUnpure(Object[] objects) {
		pureCallDepth.set(Integer.valueOf(pureCallDepth.get().intValue() - 1));
		removeFromUnpureCache(objects);
	}

	public static void unregisterUnchangeable(Object[] objects) {
		removeFromUnpureCache(objects);
	}

	private static void removeFromUnpureCache(Object[] objects) {
		unpureCache.get().removeAll(Arrays.asList(objects));
		for (Object obj : objects) {
			if (obj instanceof Object[]) {
				removeFromUnpureCache((Object[]) obj);
			}
		}
	}

	public static void checkUnpureAccess(Object target) {
		if (unpureCache.get().contains(target)) {
			AssertionError assertionError = new AssertionError("illegal access on unpure method or field");
			logger.error(assertionError.getMessage(), assertionError);
			throw assertionError;
		}
	}

	public static void checkExternalAccess(Object target, String method) {
		if (unpureCache.get().contains(target)) {
			warnExternalAccess(method);
		}
	}

	public static void warnExternalAccess(String method) {
		logger.warn("Access on unknown method " + method
				+ " outside the root-packages. Add it to the pure-registry in the configuration.");
	}

	public static void checkExternalBlacklistAccess(Object target, String method) {
		if (unpureCache.get().contains(target)) {
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
}
