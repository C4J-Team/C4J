package de.andrena.next.internal.evaluator;

import java.util.Arrays;

import org.apache.log4j.Logger;

import de.andrena.next.internal.compiler.StaticCall;
import de.andrena.next.internal.util.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
	public static final StaticCall unregisterUnpure = new StaticCall(PureEvaluator.class, "unregisterUnpure");
	public static final StaticCall checkUnpureAccess = new StaticCall(PureEvaluator.class, "checkUnpureAccess");
	public static final StaticCall checkExternalAccess = new StaticCall(PureEvaluator.class, "checkExternalAccess");
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
		logger.warn("access on unpure method " + method
				+ " outside the root-packages. add it to the white- or blacklist in the configuration.");
	}

	public static void checkUnpureStatic() {
		if (pureCallDepth.get().intValue() > 0) {
			throw new AssertionError("illegal method access on unpure static method or field");
		}
	}
}
