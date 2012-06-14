package de.vksi.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public abstract class ContractDeclaredBehaviorTransformer extends AbstractContractClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		for (CtBehavior contractBehavior : currentContractClass.getDeclaredBehaviors()) {
			if (logger.isTraceEnabled()) {
				logger.trace("transforming behavior " + contractBehavior.getLongName());
			}
			transform(contractInfo, contractBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception;

}
