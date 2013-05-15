package de.vksi.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.contracts.ContractInfo;

public abstract class ContractDeclaredBehaviorTransformer extends AbstractContractClassTransformer {
	private static final Logger LOGGER = Logger.getLogger(ContractDeclaredBehaviorTransformer.class);

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		for (CtBehavior contractBehavior : currentContractClass.getDeclaredBehaviors()) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("transforming behavior " + contractBehavior.getLongName());
			}
			transform(contractInfo, contractBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception;

}
