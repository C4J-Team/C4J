package de.andrena.next.internal.transformer;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.ListOrderedSet;
import de.andrena.next.internal.util.PureInspector;
import de.andrena.next.internal.util.ReflectionHelper;

public class PureTransformer extends AbstractAffectedClassTransformer {

	private PureInspector pureInspector;
	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	public PureTransformer(RootTransformer rootTransformer) {
		this.pureInspector = new PureInspector(rootTransformer);
	}

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredModifiableMethods(affectedClass)) {
			CtBehavior pureBehavior = pureInspector.inspect(involvedClasses, affectedBehavior);
			if (pureBehavior != null) {
				addBehaviorAnnotation(affectedBehavior, Pure.class);
				logger.info("added @Pure from " + pureBehavior.getLongName() + " to " + affectedBehavior.getLongName());
			}
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				pureInspector.verify(affectedBehavior, false);
			} else {
				pureInspector.checkUnpureAccess(affectedBehavior);
			}
		}
	}

	private void addBehaviorAnnotation(CtBehavior targetBehavior, Class<?> annotationClass) throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) targetBehavior.getMethodInfo().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(targetBehavior.getMethodInfo().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			targetBehavior.getMethodInfo().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(targetBehavior.getMethodInfo()
				.getConstPool(), ClassPool.getDefault().get(annotationClass.getName())));
	}
}
