package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractAffectedClassTransformer extends ClassTransformer {
	public abstract void transform(ContractInfo contract, CtClass affectedClass) throws Exception;
}
