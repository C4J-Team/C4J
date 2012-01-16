package de.andrena.next.internal.transformer;

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
				CtBehavior involvedBehavior;
				if (affectedBehavior instanceof CtConstructor) {
					try {
						involvedBehavior = involvedClass.getDeclaredConstructor(affectedBehavior.getParameterTypes());
					} catch (NotFoundException e) {
						continue;
					}
				} else {
					try {
						involvedBehavior = involvedClass.getDeclaredMethod(affectedBehavior.getName(),
								affectedBehavior.getParameterTypes());
					} catch (NotFoundException e) {
						continue;
					}
				}
				if (involvedBehavior.hasAnnotation(Pure.class)) {
					addBehaviorAnnotation(affectedBehavior, Pure.class);
				}
			}
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				verifyPure(affectedBehavior);
			}
		}
	}

	private void verifyPure(CtBehavior affectedBehavior) throws CannotCompileException {
		affectedBehavior.instrument(new PureMethodExpressionEditor(affectedBehavior));
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
