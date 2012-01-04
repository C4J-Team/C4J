package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractContractClassTransformer extends ClassTransformer {
	public abstract void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception;
}
