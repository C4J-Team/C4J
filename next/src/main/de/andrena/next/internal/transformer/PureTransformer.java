package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.Pure;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.HelperFactory;
import de.andrena.next.internal.util.ListOrderedSet;
import de.andrena.next.internal.util.PureInspector;

public class PureTransformer extends AbstractAffectedClassTransformer {

	private PureInspector pureInspector = new PureInspector();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : HelperFactory.getReflectionHelper().getDeclaredModifiableBehaviors(
				affectedClass)) {
			CtBehavior pureBehavior = pureInspector.inspect(involvedClasses, affectedBehavior);
			if (pureBehavior != null) {
				HelperFactory.getTransformationHelper().addBehaviorAnnotation(affectedBehavior, Pure.class);
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
