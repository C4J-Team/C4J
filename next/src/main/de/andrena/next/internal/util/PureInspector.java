package de.andrena.next.internal.util;

import java.util.Set;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import de.andrena.next.Pure;

public class PureInspector {
	public CtBehavior inspect(Set<CtClass> involvedClasses, CtBehavior behavior) {
		for (CtClass involvedClass : involvedClasses) {
			CtBehavior involvedBehavior = getInvolvedBehavior(behavior, involvedClass);
			if (involvedBehavior != null && involvedBehavior.hasAnnotation(Pure.class)) {
				return involvedBehavior;
			}
		}
		return null;
	}

	private CtBehavior getInvolvedBehavior(CtBehavior affectedBehavior, CtClass involvedClass) {
		if (affectedBehavior instanceof CtConstructor) {
			try {
				return involvedClass.getDeclaredConstructor(affectedBehavior.getParameterTypes());
			} catch (NotFoundException e) {
				return null;
			}
		}
		try {
			return involvedClass.getDeclaredMethod(affectedBehavior.getName(), affectedBehavior.getParameterTypes());
		} catch (NotFoundException e) {
			return null;
		}
	}
}
