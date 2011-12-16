package de.andrena.next.internal.util;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;

import org.apache.log4j.Logger;

public class BackdoorAnnotationLoader {
	private AnnotationsAttribute annotationsAttribute;
	private static final Logger logger = Logger.getLogger(BackdoorAnnotationLoader.class);

	public BackdoorAnnotationLoader(CtClass clazz) {
		annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile()
				.getAttribute(AnnotationsAttribute.invisibleTag);
	}

	public BackdoorAnnotationLoader(CtBehavior behavior) {
		annotationsAttribute = (AnnotationsAttribute) behavior.getMethodInfo().getAttribute(
				AnnotationsAttribute.invisibleTag);
	}

	public Annotation loadAnnotation(Class<? extends java.lang.annotation.Annotation> annotationClass) {
		if (annotationsAttribute == null) {
			return null;
		}
		return annotationsAttribute.getAnnotation(annotationClass.getName());
	}

	public String getClassValue(Class<? extends java.lang.annotation.Annotation> annotationClass, String key) {
		Annotation annotation = loadAnnotation(annotationClass);
		if (annotation == null) {
			return null;
		}
		MemberValue memberValue = annotation.getMemberValue(key);
		if (memberValue == null || !(memberValue instanceof ClassMemberValue)) {
			logger.fatal(annotationClass.getName() + " did not include '" + key
					+ "' and/or it's not a value of type Class.");
			return null;
		}
		return ((ClassMemberValue) memberValue).getValue();
	}

	public String[] getClassArrayValue(Class<? extends java.lang.annotation.Annotation> annotationClass, String key) {
		Annotation annotation = loadAnnotation(annotationClass);
		if (annotation == null) {
			return null;
		}
		MemberValue memberValue = annotation.getMemberValue(key);
		if (memberValue == null || !(memberValue instanceof ArrayMemberValue)) {
			logger.fatal(annotationClass.getName() + " did not include '" + key
					+ "' and/or it's not a value of type Array.");
			return null;
		}
		MemberValue[] arrayValue = ((ArrayMemberValue) memberValue).getValue();
		String[] values = new String[arrayValue.length];
		for (int i = 0; i < arrayValue.length; i++) {
			if (arrayValue[i] == null || !(arrayValue[i] instanceof ClassMemberValue)) {
				logger.fatal(annotationClass.getName() + " is not a value of type Class-Array.");
				return null;
			}
			values[i] = ((ClassMemberValue) arrayValue[i]).getValue();
		}
		return values;
	}
}
