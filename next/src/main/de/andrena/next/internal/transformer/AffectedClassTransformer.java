package de.andrena.next.internal.transformer;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.Transformed;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.ListOrderedSet;
import de.andrena.next.internal.util.TransformationHelper;

public class AffectedClassTransformer extends AbstractAffectedClassTransformer {
	private Logger logger = Logger.getLogger(getClass());
	private TransformationHelper transformationHelper = new TransformationHelper();
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			// beware: PureTransformer has to run first!
			new PureTransformer(), new ConditionAndInvariantTransformer() };

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		logger.info("transforming class " + affectedClass.getName());
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass);
		}
		transformationHelper.addClassAnnotation(affectedClass,
				RootTransformer.INSTANCE.getPool().get(Transformed.class.getName()));
	}
}
