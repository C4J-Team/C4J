package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import de.andrena.next.internal.ContractInfo;

public abstract class ContractDeclaredBehaviorTransformer extends ClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo) throws Exception {
		for (CtBehavior contractBehavior : contractInfo.getContractClass().getDeclaredBehaviors()) {
			logger.info("transforming behavior " + contractBehavior.getLongName());
			transform(contractInfo, contractBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception;

}
