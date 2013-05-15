package de.vksi.c4j.internal.util;

import de.vksi.c4j.internal.types.ListOrderedSet;
import javassist.CtClass;
import javassist.NotFoundException;

public class InvolvedTypeInspector {
	public ListOrderedSet<CtClass> inspect(CtClass type) throws NotFoundException {
		ListOrderedSet<CtClass> inheritedTypes = new ListOrderedSet<CtClass>();
		inheritedTypes.add(type);
		if (type.getSuperclass() != null) {
			inheritedTypes.addAll(inspect(type.getSuperclass()));
		}
		for (CtClass interfaze : type.getInterfaces()) {
			inheritedTypes.addAll(inspect(interfaze));
		}
		inheritedTypes.reverseOrder();
		return inheritedTypes;
	}
}
