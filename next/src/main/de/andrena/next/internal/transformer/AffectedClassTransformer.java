package de.andrena.next.internal.transformer;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.Transformed;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.HelperFactory;
import de.andrena.next.internal.util.ListOrderedSet;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private AbstractAffectedClassTransformer[] transformers;
	private Logger logger = Logger.getLogger(getClass());

	public AffectedClassTransformer(RootTransformer rootTransformer) {
		transformers = new AbstractAffectedClassTransformer[] {
				// beware: PureTransformer has to run first!
				new PureTransformer(rootTransformer), new BeforeAndAfterTriggerTransformer(rootTransformer),
				new ClassInvariantTransformer() };
	}

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		logger.info("transforming class " + affectedClass.getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass);
		}
		HelperFactory.getTransformationHelper().addClassAnnotation(affectedClass, Transformed.class);
	}
}
