package de.andrena.next.internal.util;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;

public class TransformationHelper {
	private ClassPool pool;

	/**
	 * Instantiated by ReflectionHelper.
	 */
	TransformationHelper(ClassPool pool) {
		this.pool = pool;
	}

	public void addClassAnnotation(CtClass targetClass, Class<?> annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetClass.getClassFile().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetClass.getClassFile().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetClass.getClassFile().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetClass.getClassFile()
				.getConstPool(), pool.get(annotationClass.getName())));
	}

	public void addBehaviorAnnotation(CtBehavior targetBehavior, Class<?> annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetBehavior.getMethodInfo().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetBehavior.getMethodInfo().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetBehavior.getMethodInfo().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetBehavior.getMethodInfo()
				.getConstPool(), pool.get(annotationClass.getName())));
	}
}
