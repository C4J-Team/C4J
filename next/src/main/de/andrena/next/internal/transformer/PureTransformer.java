package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
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
			if (isPure(involvedClasses, contracts, affectedBehavior)) {
				pureInspector.verify(affectedBehavior, false);
			} else {
				pureInspector.checkUnpureAccess(affectedBehavior);
			}
		}
	}

	private boolean isPure(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtBehavior behavior) throws NotFoundException {
		if (behavior instanceof CtConstructor) {
			return false;
		}
		CtMethod method = (CtMethod) behavior;
		CtMethod pureOrigin = pureInspector.getPureOrigin(involvedClasses, contracts, method);
		if (pureOrigin != null) {
			transformationHelper.addBehaviorAnnotation(method,
					RootTransformer.INSTANCE.getPool().get(Pure.class.getName()));
			logger.info("added @Pure from " + pureOrigin.getLongName() + " to " + method.getLongName());
		}
		return method.hasAnnotation(Pure.class);
	}

}
