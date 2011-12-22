package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public abstract class ContractDeclaredBehaviorTransformer extends AbstractContractClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtClass currentContractClass) throws Exception {
		for (CtBehavior contractBehavior : currentContractClass.getDeclaredBehaviors()) {
			logger.info("transforming behavior " + contractBehavior.getLongName());
			transform(contractInfo, contractBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception;

}
