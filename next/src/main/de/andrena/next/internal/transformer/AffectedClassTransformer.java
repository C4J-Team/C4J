package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			new BeforeAndAfterTriggerTransformer(), new ClassInvariantTransformer() };

	protected AbstractAffectedClassTransformer[] getTransformers() {
		return transformers;
	}

	@Override
	public void transform(ContractInfo contract, CtClass affectedClass) throws Exception {
		logger.info("transforming class " + affectedClass.getName() + " with contract-class "
				+ contract.getContractClass().getName() + " from target-class " + contract.getTargetClass().getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(contract, affectedClass);
		}
	}

}
