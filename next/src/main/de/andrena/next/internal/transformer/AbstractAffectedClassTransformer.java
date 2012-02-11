package de.andrena.next.internal.transformer;

import java.util.List;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(List<CtClass> involvedClasses, List<ContractInfo> contracts, CtClass affectedClass)
			throws Exception;
}
