package de.andrena.next.internal.util;

import javassist.ClassPool;

public enum HelperFactory {
	INSTANCE;

	private TransformationHelper transformationHelper;
	private ReflectionHelper reflectionHelper;

	private HelperFactory() {
		reflectionHelper = new ReflectionHelper();
		transformationHelper = new TransformationHelper(ClassPool.getDefault());
	}

	public static void init(ClassPool pool) {
		INSTANCE.transformationHelper = new TransformationHelper(pool);
	}

	public static TransformationHelper getTransformationHelper() {
		return INSTANCE.transformationHelper;
	}

	public static ReflectionHelper getReflectionHelper() {
		return INSTANCE.reflectionHelper;
	}
}
