package de.andrena.next.internal.util;

import javassist.CtClass;
import javassist.NotFoundException;

public class InvolvedTypeInspector {
	/**
	 * Instantiated by HelperFactory.
	 */
	InvolvedTypeInspector() {
	}

	public ListOrderedSet<CtClass> inspect(CtClass type) throws NotFoundException {
		ListOrderedSet<CtClass> inheritedTypes = new ListOrderedSet<CtClass>();
		inheritedTypes.add(type);
		if (type.getSuperclass() != null) {
			inheritedTypes.addAll(inspect(type.getSuperclass()));
		}
		for (CtClass interfaze : type.getInterfaces()) {
			inheritedTypes.addAll(inspect(interfaze));
		}
		return inheritedTypes;
	}
}
