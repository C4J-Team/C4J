package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import static de.vksi.c4j.internal.util.ReflectionHelper.getDeclaredBehaviors;
import static de.vksi.c4j.internal.util.TransformationHelper.addBehaviorAnnotation;

import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.Pure;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ContractRegistry.ContractMethod;
import de.vksi.c4j.internal.util.ListOrderedSet;
import de.vksi.c4j.internal.util.PureInspector;

public class PureTransformer extends AbstractAffectedClassTransformer {
	private PureInspector pureInspector = new PureInspector();
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass, Map<CtBehavior, List<ContractMethod>> contractMap) throws Exception {
		for (CtBehavior affectedBehavior : getDeclaredBehaviors(affectedClass, MODIFIABLE)) {
			normalizePure(affectedClass, involvedClasses, contracts, affectedBehavior);
			applyPure(affectedClass, affectedBehavior, contracts);
		}
	}

	private void applyPure(CtClass affectedClass, CtBehavior affectedBehavior, ListOrderedSet<ContractInfo> contracts)
			throws CannotCompileException, NotFoundException {
		if (rootTransformer.getXmlConfiguration().getConfiguration(affectedClass).isPureValidate()) {
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				pureInspector.verify((CtMethod) affectedBehavior, false);
			} else {
				pureInspector.checkUnpureAccess(affectedBehavior);
				pureInspector.verifyUnchangeable(affectedBehavior, contracts);
			}
		}
	}

	private void normalizePure(CtClass affectedClass, ListOrderedSet<CtClass> involvedClasses,
			ListOrderedSet<ContractInfo> contracts, CtBehavior behavior) throws NotFoundException {
		if (behavior instanceof CtConstructor) {
			return;
		}
		CtMethod method = (CtMethod) behavior;
		CtMethod pureOrigin = pureInspector.getPureOrigin(involvedClasses, contracts, method);
		if (pureOrigin != null) {
			addBehaviorAnnotation(method, ClassFilePool.INSTANCE.getClass(Pure.class));
			if (logger.isDebugEnabled()) {
				logger.debug("added @Pure from " + pureOrigin.getLongName() + " to " + method.getLongName());
			}
		}
	}

}
