package de.andrena.next.internal.transformer;

import java.util.Set;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.Transformed;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private AbstractAffectedClassTransformer[] transformers;
	private Logger logger = Logger.getLogger(getClass());

	public AffectedClassTransformer(RootTransformer rootTransformer) {
		transformers = new AbstractAffectedClassTransformer[] {
				// beware: PureTransformer has to run first!
				new PureTransformer(rootTransformer), new BeforeAndAfterTriggerTransformer(),
				new ClassInvariantTransformer() };
	}

	@Override
	public void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		logger.info("transforming class " + affectedClass.getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass);
		}
		addClassAnnotation(affectedClass, Transformed.class);
	}
}
