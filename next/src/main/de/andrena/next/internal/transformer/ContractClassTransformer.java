package de.andrena.next.internal.transformer;

import javassist.ClassPool;

public class ContractClassTransformer extends DelegateTransformer {

	private ClassTransformer[] transformers;

	public ContractClassTransformer(ClassPool pool) {
		this.transformers = new ClassTransformer[] { new ContractExpressionTransformer(pool) };
	}

	@Override
	protected ClassTransformer[] getTransformers() {
		return transformers;
	}

}
