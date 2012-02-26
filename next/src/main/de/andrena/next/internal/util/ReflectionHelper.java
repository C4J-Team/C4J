package de.andrena.next.internal.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.internal.transformer.ContractBehaviorTransformer;

public class ReflectionHelper {
	/**
	 * Instantiated by HelperFactory.
	 */
	ReflectionHelper() {
	}

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
}
