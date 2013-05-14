package de.vksi.c4j.internal.runtime;

import de.vksi.c4j.internal.compiler.StaticCall;

public class UnchangedCache {
	public static final StaticCall isUnchanged = new StaticCall(UnchangedCache.class, "isUnchanged");
	public static final StaticCall setClassInvariantConstructorCall = new StaticCall(UnchangedCache.class,
			"setClassInvariantConstructorCall");

	private final static ThreadLocal<Object> unchangedCache = new ThreadLocal<Object>();
	private final static ThreadLocal<Boolean> classInvariantConstructorCall = new ThreadLocal<Boolean>();

	public static boolean isUnchanged(Object compareObject, boolean triggerSetUnchangedCache) {
		if (isClassInvariantConstructorCall()) {
			return true;
		}
		// auto-boxing is evil, requires equals instead of ==
		if (compareObject instanceof Boolean || compareObject instanceof Byte || compareObject instanceof Character
				|| compareObject instanceof Double || compareObject instanceof Float
				|| compareObject instanceof Integer || compareObject instanceof Long || compareObject instanceof Short) {
			return compareObject.equals(unchangedCache.get());
		}
		return compareObject == unchangedCache.get();
	}

	public static void setUnchangedCache(Object value) {
		unchangedCache.set(value);
	}

	public static boolean isClassInvariantConstructorCall() {
		if (classInvariantConstructorCall.get() == null) {
			return false;
		}
		return classInvariantConstructorCall.get().booleanValue();
	}

	public static void setClassInvariantConstructorCall(boolean constructorCall) {
		classInvariantConstructorCall.set(Boolean.valueOf(constructorCall));
	}
}
