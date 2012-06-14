package de.vksi.c4j.internal.util;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import de.vksi.c4j.internal.compiler.StaticCall;

public class TransformationHelper {
	public void addClassAnnotation(CtClass targetClass, CtClass annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetClass.getClassFile().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetClass.getClassFile().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetClass.getClassFile().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetClass.getClassFile()
				.getConstPool(), annotationClass));
	}

	public void addBehaviorAnnotation(CtBehavior targetBehavior, CtClass annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetBehavior.getMethodInfo().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetBehavior.getMethodInfo().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetBehavior.getMethodInfo().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetBehavior.getMethodInfo()
				.getConstPool(), annotationClass));
	}

	public void setMethodIndex(ConstPool constPool, byte[] bytes, int index, StaticCall staticCall, String descriptor) {
		int classIndex = constPool.addClassInfo(staticCall.getCallClass().getName());
		int methodInfoIndex = constPool.addMethodrefInfo(classIndex,
				staticCall.getCallMethod(), descriptor);
		bytes[index] = (byte) (methodInfoIndex >>> 8);
		bytes[index + 1] = (byte) methodInfoIndex;
	}
}
