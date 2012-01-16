package de.andrena.next.internal.transformer;

import java.util.Collection;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(Collection<ContractInfo> contractInfo, CtClass affectedClass) throws Exception;
}
