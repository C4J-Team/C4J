package de.andrena.next.internal.transformer;

import java.util.Set;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception;
}
