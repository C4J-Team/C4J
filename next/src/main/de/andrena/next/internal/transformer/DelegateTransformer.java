package de.andrena.next.internal.transformer;

import de.andrena.next.internal.ContractInfo;

public abstract class DelegateTransformer extends ClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo) throws Exception {
		for (ClassTransformer transformer : getTransformers()) {
			transformer.transform(contractInfo);
		}
	}

	protected abstract ClassTransformer[] getTransformers();
}
