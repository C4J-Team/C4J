package de.andrena.c4j.internal.transformer;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.Transformed;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.TransformationHelper;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private Logger logger = Logger.getLogger(getClass());
	private TransformationHelper transformationHelper = new TransformationHelper();
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			// beware: PureTransformer has to run first!
			new PureTransformer(), new ConditionAndInvariantTransformer() };

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("transforming class " + affectedClass.getName());
		}
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass);
		}
		transformationHelper.addClassAnnotation(affectedClass,
				RootTransformer.INSTANCE.getPool().get(Transformed.class.getName()));
	}
}
