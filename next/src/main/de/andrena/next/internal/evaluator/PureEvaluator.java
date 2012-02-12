package de.andrena.next.internal.evaluator;

import java.util.Arrays;

import de.andrena.next.internal.compiler.StaticCall;
import de.andrena.next.internal.util.ObjectIdentitySet;

public class PureEvaluator {
	public static final StaticCall registerUnpure = new StaticCall(PureEvaluator.class, "registerUnpure");
	public static final StaticCall unregisterUnpure = new StaticCall(PureEvaluator.class, "unregisterUnpure");
	public static final StaticCall checkUnpureAccess = new StaticCall(PureEvaluator.class, "checkUnpureAccess");

	private static ThreadLocal<ObjectIdentitySet> unpureCache = new ThreadLocal<ObjectIdentitySet>() {
		@Override
		protected ObjectIdentitySet initialValue() {
			return new ObjectIdentitySet();
		}
	};

	public static void registerUnpure(Object[] objects) {
		unpureCache.get().addAll(Arrays.asList(objects));
	}

	public static void unregisterUnpure(Object[] objects) {
		unpureCache.get().removeAll(Arrays.asList(objects));
	}

	public static void checkUnpureAccess(Object target) {
		if (unpureCache.get().contains(target)) {
			throw new AssertionError("illegal method access on unpure method");
		}
	}
}
