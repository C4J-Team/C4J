package de.andrena.next.internal.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;

import org.apache.log4j.Logger;

public abstract class ClassTransformer {
	protected Logger logger = Logger.getLogger(getClass());

	protected void addClassAnnotation(CtClass targetClass, Class<?> annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetClass.getClassFile().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetClass.getClassFile().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetClass.getClassFile().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetClass.getClassFile()
				.getConstPool(), ClassPool.getDefault().get(annotationClass.getName())));
	}
}
