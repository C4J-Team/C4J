package de.andrena.c4j.internal.evaluator;

import java.util.Arrays;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.util.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
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
		unpureCache.get().addAll(Arrays.asList(objects));
	}

	public static void unregisterUnpure(Object[] objects) {
		pureCallDepth.set(Integer.valueOf(pureCallDepth.get().intValue() - 1));
		unpureCache.get().removeAll(Arrays.asList(objects));
	}

	public static void checkUnpureAccess(Object target) {
		if (unpureCache.get().contains(target)) {
			throw new AssertionError("illegal access on unpure method or field");
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
			throw new AssertionError("illegal access on unpure method " + method
					+ " outside the root-packages.");
		}
	}

	public static void checkUnpureStatic() {
		if (pureCallDepth.get().intValue() > 0) {
			throw new AssertionError("illegal method access on unpure static method or field");
		}
	}
}
