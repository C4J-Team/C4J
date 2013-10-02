package de.vksi.c4j.internal.transformer.util;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.isDynamic;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.isStatic;

import java.lang.ref.WeakReference;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.internal.classfile.ClassFilePool;

public class ContractClassMemberHelper {
	private static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before".toString();
	public static final String TARGET_FIELD_NAME = "target$".toString();

	public static boolean isContractConstructor(CtBehavior contractBehavior) {
		return contractBehavior.hasAnnotation(ConstructorContract.class) && isDynamic(contractBehavior);
	}

	public static boolean isContractClassInitializer(CtBehavior contractBehavior) {
		return contractBehavior.hasAnnotation(ConstructorContract.class) && isStatic(contractBehavior);
	}

	public static boolean isClassInvariant(CtBehavior contractBehavior) {
		return contractBehavior.hasAnnotation(ClassInvariant.class);
	}

	public static boolean isBeforeInvariantHelperMethod(CtBehavior contractBehavior) {
		return contractBehavior.getName().endsWith(BEFORE_INVARIANT_METHOD_SUFFIX);
	}

	public static String makeBeforeInvariantHelperMethodName(CtMethod contractMethod) {
		return contractMethod.getName() + BEFORE_INVARIANT_METHOD_SUFFIX;
	}

	public static CtField createWeakTargetField(CtClass contractClass) throws NotFoundException, CannotCompileException {
		CtClass weakReferenceClass = ClassFilePool.INSTANCE.getClass(WeakReference.class);
		return new CtField(weakReferenceClass, TARGET_FIELD_NAME, contractClass);
	}

}
