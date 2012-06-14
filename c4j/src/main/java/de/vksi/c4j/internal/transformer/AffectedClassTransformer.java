package de.vksi.c4j.internal.transformer;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.Transformed;
import de.vksi.c4j.internal.util.ListOrderedSet;
import de.vksi.c4j.internal.util.TransformationHelper;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private Logger logger = Logger.getLogger(getClass());
	private TransformationHelper transformationHelper = new TransformationHelper();
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			// beware: PureTransformer has to run first!
			// InvariantTransformer has to run after DynamicConditionTransformer
			new PureTransformer(), new DynamicConditionTransformer(), new InvariantTransformer(),
			new StaticConditionTransformer() };

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
