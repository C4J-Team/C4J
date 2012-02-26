package de.andrena.next.internal.util;

import javassist.ClassPool;

public enum HelperFactory {
	INSTANCE;

	private TransformationHelper transformationHelper;
	private ReflectionHelper reflectionHelper;
	private InvolvedTypeInspector involvedTypeInspector;
	private AffectedBehaviorLocator affectedBehaviorLocator;

	private HelperFactory() {
		reflectionHelper = new ReflectionHelper();
		transformationHelper = new TransformationHelper(ClassPool.getDefault());
		involvedTypeInspector = new InvolvedTypeInspector();
		affectedBehaviorLocator = new AffectedBehaviorLocator(reflectionHelper, involvedTypeInspector);
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

	public static InvolvedTypeInspector getInvolvedTypeInspector() {
		return INSTANCE.involvedTypeInspector;
	}

	public static AffectedBehaviorLocator getAffectedBehaviorLocator() {
		return INSTANCE.affectedBehaviorLocator;
	}
}
