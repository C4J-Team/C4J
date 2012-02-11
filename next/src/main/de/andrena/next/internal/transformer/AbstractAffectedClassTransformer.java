package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.ListOrderedSet;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception;
}
