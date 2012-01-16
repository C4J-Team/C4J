package de.andrena.next.internal.transformer;

import java.util.Set;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AffectedClassTransformerForSingleContract extends AbstractAffectedClassTransformer {

	@Override
	public void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		for (ContractInfo contractInfo : contracts) {
			logger.info("transforming class " + affectedClass.getName() + " with contract-class "
					+ contractInfo.getContractClass().getName() + " from target-class "
					+ contractInfo.getTargetClass().getName());
			transform(contractInfo, affectedClass);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception;

}
