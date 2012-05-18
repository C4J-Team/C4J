package de.andrena.c4j.internal.transformer;

import javassist.CtClass;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception;
}
