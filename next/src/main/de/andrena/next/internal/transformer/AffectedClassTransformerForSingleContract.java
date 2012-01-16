package de.andrena.next.internal.transformer;

import java.util.Collection;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AffectedClassTransformerForSingleContract extends AbstractAffectedClassTransformer {

	@Override
	public void transform(Collection<ContractInfo> contractInfos, CtClass affectedClass) throws Exception {
		for (ContractInfo contractInfo : contractInfos) {
			logger.info("transforming class " + affectedClass.getName() + " with contract-class "
					+ contractInfo.getContractClass().getName() + " from target-class "
					+ contractInfo.getTargetClass().getName());
			transform(contractInfo, affectedClass);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception;

}
