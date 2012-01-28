package de.andrena.next.internal.util;

import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.NotFoundException;

public class InvolvedTypeInspector {
	public Set<CtClass> inspect(CtClass type) throws NotFoundException {
		Set<CtClass> inheritedTypes = new HashSet<CtClass>();
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
