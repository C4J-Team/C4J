package de.andrena.next.internal.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;

public class ReflectionHelper {
	public List<CtMethod> getDeclaredModifiableMethods(CtClass clazz) {
		CtMethod[] declaredMethods = clazz.getDeclaredMethods();
		List<CtMethod> declaredModifiableMethods = new ArrayList<CtMethod>(declaredMethods.length);
		for (CtMethod method : declaredMethods) {
			if (isModifiable(method)) {
				declaredModifiableMethods.add(method);
			}
		}
		return declaredModifiableMethods;
	}

	public List<CtMethod> getDeclaredModifiableDynamicMethods(CtClass clazz) {
		CtMethod[] declaredMethods = clazz.getDeclaredMethods();
		List<CtMethod> declaredModifiableMethods = new ArrayList<CtMethod>(declaredMethods.length);
		for (CtMethod method : declaredMethods) {
			if (isModifiable(method) && isDynamic(method)) {
				declaredModifiableMethods.add(method);
			}
		}
		return declaredModifiableMethods;
	}

	public boolean isModifiable(CtBehavior behavior) {
		return !Modifier.isNative(behavior.getModifiers()) && !Modifier.isAbstract(behavior.getModifiers());
	}

	public boolean isDynamic(CtBehavior behavior) {
		return !Modifier.isStatic(behavior.getModifiers());
	}
}
