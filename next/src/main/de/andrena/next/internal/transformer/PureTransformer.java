package de.andrena.next.internal.transformer;

import java.lang.annotation.Annotation;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import de.andrena.next.Pure;
import de.andrena.next.internal.editor.PureMethodExpressionEditor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class PureTransformer extends AbstractAffectedClassTransformer {

	@Override
	public void transform(Set<CtClass> involvedClasses, Set<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		for (CtBehavior affectedBehavior : affectedClass.getDeclaredBehaviors()) {
			for (CtClass involvedClass : involvedClasses) {
				CtBehavior involvedBehavior = getBehaviorFromType(affectedBehavior, involvedClass);
				if (involvedBehavior != null && involvedBehavior.hasAnnotation(Pure.class)) {
					addBehaviorAnnotation(affectedBehavior, Pure.class);
				}
			}
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				verifyPure(affectedBehavior);
			}
		}
	}

	private CtBehavior getBehaviorFromType(CtBehavior behavior, CtClass type) {
		try {
			if (behavior instanceof CtConstructor) {
				return type.getDeclaredConstructor(behavior.getParameterTypes());
			} else {
				return type.getDeclaredMethod(behavior.getName(), behavior.getParameterTypes());
			}
		} catch (NotFoundException e) {
			return null;
		}
	}

	private void verifyPure(CtBehavior affectedBehavior) throws CannotCompileException {
		affectedBehavior.instrument(new PureMethodExpressionEditor(affectedBehavior));
	}

	private void addBehaviorAnnotation(CtBehavior behavior, Class<? extends Annotation> annotationClass)
			throws NotFoundException {
		AnnotationsAttribute targetAttribute = (AnnotationsAttribute) behavior.getMethodInfo().getAttribute(
				AnnotationsAttribute.invisibleTag);
		if (targetAttribute == null) {
			targetAttribute = new AnnotationsAttribute(behavior.getMethodInfo().getConstPool(),
					AnnotationsAttribute.invisibleTag);
			behavior.getMethodInfo().addAttribute(targetAttribute);
		}
		targetAttribute.addAnnotation(new javassist.bytecode.annotation.Annotation(behavior.getMethodInfo()
				.getConstPool(), ClassPool.getDefault().get(annotationClass.getName())));
	}

}
