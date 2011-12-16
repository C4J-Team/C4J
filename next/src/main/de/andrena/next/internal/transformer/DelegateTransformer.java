package de.andrena.next.internal.transformer;

import javassist.CtClass;

public abstract class DelegateTransformer extends ClassTransformer {

	@Override
	public void transform(CtClass targetClass, CtClass contractClass) throws Exception {
		for (ClassTransformer transformer : getTransformers()) {
			transformer.transform(targetClass, contractClass);
		}
	}

	protected abstract ClassTransformer[] getTransformers();
}
