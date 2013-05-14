package de.vksi.c4j.internal.classfile;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;

public class ClassAnalyzer {
	private static <T extends CtBehavior> List<T> filterBehaviors(T[] behaviors, BehaviorFilter... filters) {
		List<T> filteredList = new ArrayList<T>(behaviors.length);
		behaviorLoop: for (T behavior : behaviors) {
			for (BehaviorFilter filter : filters) {
				if (!filter.filter(behavior)) {
					continue behaviorLoop;
				}
			}
			filteredList.add(behavior);
		}
		return filteredList;
	}

	public static List<CtMethod> getDeclaredMethods(CtClass clazz, BehaviorFilter... filters) {
		return filterBehaviors(clazz.getDeclaredMethods(), filters);
	}

	public static List<CtBehavior> getDeclaredBehaviors(CtClass clazz, BehaviorFilter... filters) {
		return filterBehaviors(clazz.getDeclaredBehaviors(), filters);
	}

	public static boolean isModifiable(CtBehavior behavior) {
		return !Modifier.isNative(behavior.getModifiers()) && !Modifier.isAbstract(behavior.getModifiers());
	}

	public static boolean isDynamic(CtBehavior behavior) {
		return !Modifier.isStatic(behavior.getModifiers());
	}

	public static boolean isPrivate(CtBehavior behavior) {
		return Modifier.isPrivate(behavior.getModifiers());
	}

	public static boolean constructorHasAdditionalParameter(CtClass affectedClass) throws NotFoundException {
		return affectedClass.getDeclaringClass() != null && !Modifier.isStatic(affectedClass.getModifiers());
	}

	public static boolean isInitializer(CtBehavior behavior) {
		return (behavior instanceof CtConstructor && !((CtConstructor) behavior).isClassInitializer());
	}

	public static boolean isClassInitializer(CtBehavior behavior) {
		return (behavior instanceof CtConstructor && ((CtConstructor) behavior).isClassInitializer());
	}

	public static String getSimpleName(CtBehavior behavior) {
		if (behavior instanceof CtConstructor) {
			CtConstructor constructor = (CtConstructor) behavior;
			return constructor.getDeclaringClass().getSimpleName()
					+ (constructor.isConstructor() ? Descriptor.toString(constructor.getSignature()) : ("."
							+ MethodInfo.nameClinit + "()"));
		}
		return behavior.getDeclaringClass().getSimpleName() + "." + behavior.getName()
				+ Descriptor.toString(behavior.getSignature());
	}

	public static String getSimpleName(CtField field) {
		return field.getDeclaringClass().getSimpleName() + "." + field.getName();
	}

	/**
	 * Calling Class.getSimpleName() in Sun-JDK 1.6.0_24 (and possibly many other versions) for some reason loads all
	 * the other classes being defined in the class, thus possibly loading contract-classes before their corresponding
	 * target-classes are being loaded. Also, Class.getSimpleName() is empty for anonymous and local classes.
	 * <p>
	 * This is why we need our own alternative.
	 */
	public static String getSimplerName(Class<?> clazz) {
		// because of lastIndexOf() returning -1 when nothing is found and the addition of 1, this even works
		// for classes in the default package
		return clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
	}

	public static CtConstructor getDeclaredConstructor(CtClass clazz, CtClass... parameters) {
		try {
			return clazz.getDeclaredConstructor(parameters);
		} catch (NotFoundException e) {
			return null;
		}
	}

	public static CtMethod getDeclaredMethod(CtClass clazz, String methodName, CtClass... parameters) {
		try {
			return clazz.getDeclaredMethod(methodName, parameters);
		} catch (NotFoundException e) {
			return null;
		}
	}

	public static CtMethod getMethod(CtClass clazz, String methodName, String signature) {
		try {
			return clazz.getMethod(methodName, signature);
		} catch (NotFoundException e) {
			return null;
		}
	}

	public static CtField getField(CtClass clazz, String fieldName) {
		try {
			return clazz.getField(fieldName);
		} catch (NotFoundException e) {
			return null;
		}
	}
}
