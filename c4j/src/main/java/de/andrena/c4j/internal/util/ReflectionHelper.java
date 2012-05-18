package de.andrena.c4j.internal.util;

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
import de.andrena.c4j.internal.transformer.ContractBehaviorTransformer;

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

	public List<CtBehavior> getDeclaredModifiableBehaviors(CtClass clazz) {
		CtBehavior[] declaredBehaviors = clazz.getDeclaredBehaviors();
		List<CtBehavior> declaredModifiableBehaviors = new ArrayList<CtBehavior>(declaredBehaviors.length);
		for (CtBehavior behavior : declaredBehaviors) {
			if (isModifiable(behavior)) {
				declaredModifiableBehaviors.add(behavior);
			}
		}
		return declaredModifiableBehaviors;
	}

	public List<CtBehavior> getDeclaredModifiableDynamicBehaviors(CtClass clazz) {
		CtBehavior[] declaredBehaviors = clazz.getDeclaredBehaviors();
		List<CtBehavior> declaredModifiableBehaviors = new ArrayList<CtBehavior>(declaredBehaviors.length);
		for (CtBehavior behavior : declaredBehaviors) {
			if (isModifiable(behavior) && isDynamic(behavior)) {
				declaredModifiableBehaviors.add(behavior);
			}
		}
		return declaredModifiableBehaviors;
	}

	public boolean isModifiable(CtBehavior behavior) {
		return !Modifier.isNative(behavior.getModifiers()) && !Modifier.isAbstract(behavior.getModifiers());
	}

	public boolean isDynamic(CtBehavior behavior) {
		return !Modifier.isStatic(behavior.getModifiers());
	}

	public boolean constructorHasAdditionalParameter(CtClass affectedClass) throws NotFoundException {
		return affectedClass.getDeclaringClass() != null && !Modifier.isStatic(affectedClass.getModifiers());
	}

	public boolean isContractConstructor(CtBehavior contractBehavior) {
		return contractBehavior instanceof CtConstructor
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	public String getContractBehaviorName(CtBehavior contractBehavior) {
		String contractBehaviorName;
		if (isContractConstructor(contractBehavior)) {
			contractBehaviorName = ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME;
		} else {
			contractBehaviorName = contractBehavior.getName();
		}
		return contractBehaviorName;
	}

	public String getSimpleName(CtBehavior behavior) {
		if (behavior instanceof CtConstructor) {
			CtConstructor constructor = (CtConstructor) behavior;
			return constructor.getDeclaringClass().getSimpleName()
					+ (constructor.isConstructor() ? Descriptor.toString(constructor.getSignature()) : ("."
							+ MethodInfo.nameClinit + "()"));
		}
		return behavior.getDeclaringClass().getSimpleName() + "." + behavior.getName()
				+ Descriptor.toString(behavior.getSignature());
	}

	public String getSimpleName(CtField field) {
		return field.getDeclaringClass().getSimpleName() + "." + field.getName();
	}

	/**
	 * Calling Class.getSimpleName() in Sun-JDK 1.6.0_24 (and possibly many other versions) for some reason loads all
	 * the other classes being defined in the class, thus possibly loading contract-classes before their corresponding
	 * target-classes are being loaded. Also, Class.getSimpleName() is empty for anonymous and local classes.
	 * <p>
	 * This is why we need our own alternative.
	 */
	public String getSimplerName(Class<?> clazz) {
		// because of lastIndexOf() returning -1 when nothing is found and the addition of 1, this even works
		// for classes in the default package
		return clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
	}
}
