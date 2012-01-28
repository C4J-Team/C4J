package de.andrena.next.internal.transformer;

import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.editor.PureBehaviorExpressionEditor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.PureInspector;
import de.andrena.next.internal.util.PureInspectorProvider;

public class PureTransformer extends AbstractAffectedClassTransformer implements PureInspectorProvider {

	private RootTransformer rootTransformer;
	private PureInspector pureInspector = new PureInspector();

	public PureTransformer(RootTransformer rootTransformer) {
		this.rootTransformer = rootTransformer;
	}

	@Override
	public void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		for (CtBehavior affectedBehavior : affectedClass.getDeclaredBehaviors()) {
			CtBehavior pureBehavior = pureInspector.inspect(involvedClasses, affectedBehavior);
			if (pureBehavior != null) {
				addBehaviorAnnotation(affectedBehavior, Pure.class);
				logger.info("added @Pure from " + pureBehavior.getLongName() + " to " + affectedBehavior.getLongName());
			}
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				verifyPure(affectedBehavior);
			}
		}
	}

	private void verifyPure(CtBehavior affectedBehavior) throws CannotCompileException {
		affectedBehavior.instrument(new PureBehaviorExpressionEditor(affectedBehavior, rootTransformer, this, false));
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

	@Override
	public PureInspector getPureInspector() {
		return pureInspector;
	}

}
