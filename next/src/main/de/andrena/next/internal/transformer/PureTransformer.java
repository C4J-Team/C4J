package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.ListOrderedSet;
import de.andrena.next.internal.util.PureInspector;
import de.andrena.next.internal.util.ReflectionHelper;
import de.andrena.next.internal.util.TransformationHelper;

public class PureTransformer extends AbstractAffectedClassTransformer {
	private PureInspector pureInspector = new PureInspector();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();
	private TransformationHelper transformationHelper = new TransformationHelper();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredModifiableBehaviors(affectedClass)) {
			CtBehavior pureBehavior = pureInspector.inspect(involvedClasses, affectedBehavior);
			if (pureBehavior != null) {
				transformationHelper.addBehaviorAnnotation(affectedBehavior,
						RootTransformer.INSTANCE.getPool().get(Pure.class.getName()));
				logger.info("added @Pure from " + pureBehavior.getLongName() + " to " + affectedBehavior.getLongName());
			}
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				pureInspector.verify(affectedBehavior, false);
			} else {
				pureInspector.checkUnpureAccess(affectedBehavior);
			}
		}
	}

}
