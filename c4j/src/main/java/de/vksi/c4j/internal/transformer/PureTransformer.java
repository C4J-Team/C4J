package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.Pure;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ListOrderedSet;
import de.vksi.c4j.internal.util.PureInspector;
import de.vksi.c4j.internal.util.ReflectionHelper;
import de.vksi.c4j.internal.util.TransformationHelper;

public class PureTransformer extends AbstractAffectedClassTransformer {
	private PureInspector pureInspector = new PureInspector();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();
	private TransformationHelper transformationHelper = new TransformationHelper();
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredBehaviors(affectedClass, MODIFIABLE)) {
			normalizePure(involvedClasses, contracts, affectedBehavior);
			applyPure(affectedClass, affectedBehavior, contracts);
		}
	}

	private void applyPure(CtClass affectedClass, CtBehavior affectedBehavior, ListOrderedSet<ContractInfo> contracts)
			throws CannotCompileException,
			NotFoundException {
		if (rootTransformer.getXmlConfiguration().getConfiguration(affectedClass).isPureValidate()) {
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				pureInspector.verify((CtMethod) affectedBehavior, false);
			} else {
				pureInspector.checkUnpureAccess(affectedBehavior);
				pureInspector.verifyUnchangeable(affectedBehavior, contracts);
			}
		}
	}

	private void normalizePure(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtBehavior behavior) throws NotFoundException {
		if (behavior instanceof CtConstructor) {
			return;
		}
		CtMethod method = (CtMethod) behavior;
		CtMethod pureOrigin = pureInspector.getPureOrigin(involvedClasses, contracts, method);
		if (pureOrigin != null) {
			transformationHelper.addBehaviorAnnotation(method,
					RootTransformer.INSTANCE.getPool().get(Pure.class.getName()));
			if (logger.isDebugEnabled()) {
				logger.debug("added @Pure from " + pureOrigin.getLongName() + " to " + method.getLongName());
			}
		}
	}

}
