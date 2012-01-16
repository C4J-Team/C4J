package de.andrena.next.internal.transformer;

import java.util.Collection;

import javassist.CtClass;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			new BeforeAndAfterTriggerTransformer(), new ClassInvariantTransformer(), new PureTransformer() };

	protected AbstractAffectedClassTransformer[] getTransformers() {
		return transformers;
	}

	@Override
	public void transform(Collection<ContractInfo> contract, CtClass affectedClass) throws Exception {
		logger.info("transforming class " + affectedClass.getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(contract, affectedClass);
		}
	}

}
