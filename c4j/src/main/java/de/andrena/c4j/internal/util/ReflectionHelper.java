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
	private <T extends CtBehavior> List<T> filterBehaviors(T[] behaviors, BehaviorFilter... filters) {
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

	public List<CtMethod> getDeclaredMethods(CtClass clazz, BehaviorFilter... filters) {
		return filterBehaviors(clazz.getDeclaredMethods(), filters);
	}

	public List<CtBehavior> getDeclaredBehaviors(CtClass clazz, BehaviorFilter... filters) {
		return filterBehaviors(clazz.getDeclaredBehaviors(), filters);
	}

	static boolean isModifiable(CtBehavior behavior) {
		return !Modifier.isNative(behavior.getModifiers()) && !Modifier.isAbstract(behavior.getModifiers());
	}

	static boolean isDynamic(CtBehavior behavior) {
		return !Modifier.isStatic(behavior.getModifiers());
	}

	static boolean isPrivate(CtBehavior behavior) {
		return Modifier.isPrivate(behavior.getModifiers());
	}

	public boolean constructorHasAdditionalParameter(CtClass affectedClass) throws NotFoundException {
		return affectedClass.getDeclaringClass() != null && !Modifier.isStatic(affectedClass.getModifiers());
	}

	public boolean isContractConstructor(CtBehavior contractBehavior) {
		return (contractBehavior instanceof CtConstructor && !((CtConstructor) contractBehavior).isClassInitializer())
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	public boolean isContractClassInitializer(CtBehavior contractBehavior) {
		return (contractBehavior instanceof CtConstructor && ((CtConstructor) contractBehavior).isClassInitializer())
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CLASS_INITIALIZER_REPLACEMENT_NAME);
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
