package de.vksi.c4j.internal.classfile;

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
	private static final Logger LOGGER = Logger.getLogger(BackdoorAnnotationLoader.class);

	public BackdoorAnnotationLoader(CtClass clazz) {
		annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile2()
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
		if (memberValue == null) {
			return null;
		}
		if (!(memberValue instanceof ClassMemberValue)) {
			LOGGER.fatal(annotationClass.getName() + "." + key + " is not a value of type Class.");
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
		if (memberValue == null) {
			return null;
		}
		if (!(memberValue instanceof ArrayMemberValue)) {
			LOGGER.fatal(annotationClass.getName() + "." + key + " is not a value of type Array.");
			return null;
		}
		String[] values = getClassArrayValues(annotationClass.getName(), key, ((ArrayMemberValue) memberValue)
				.getValue());
		return values;
	}

	private String[] getClassArrayValues(String annotationName, String key, MemberValue[] arrayValue) {
		String[] values = new String[arrayValue.length];
		for (int i = 0; i < arrayValue.length; i++) {
			if (arrayValue[i] == null) {
				values[i] = null;
			} else if (!(arrayValue[i] instanceof ClassMemberValue)) {
				LOGGER.fatal(annotationName + "." + key + ", element " + i + " is not a value of type Class-Array.");
				values[i] = null;
			} else {
				values[i] = ((ClassMemberValue) arrayValue[i]).getValue();
			}
		}
		return values;
	}
}
