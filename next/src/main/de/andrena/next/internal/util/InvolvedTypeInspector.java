package de.andrena.next.internal.util;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.NotFoundException;

public class InvolvedTypeInspector {
	public List<CtClass> inspect(CtClass type) throws NotFoundException {
		List<CtClass> inheritedTypes = new ArrayList<CtClass>();
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
