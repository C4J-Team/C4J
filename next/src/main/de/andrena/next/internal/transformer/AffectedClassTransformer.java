package de.andrena.next.internal.transformer;

import java.util.Set;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			new BeforeAndAfterTriggerTransformer(), new ClassInvariantTransformer(), new PureTransformer() };
	private Logger logger = Logger.getLogger(getClass());

	protected AbstractAffectedClassTransformer[] getTransformers() {
		return transformers;
	}

	@Override
	public void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		logger.info("transforming class " + affectedClass.getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass);
		}
	}
}
