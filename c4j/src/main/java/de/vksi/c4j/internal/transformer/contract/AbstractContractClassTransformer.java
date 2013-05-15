package de.vksi.c4j.internal.transformer.contract;

import javassist.CtClass;
import de.vksi.c4j.internal.contracts.ContractInfo;

public abstract class AbstractContractClassTransformer {
	public abstract void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception;
}
