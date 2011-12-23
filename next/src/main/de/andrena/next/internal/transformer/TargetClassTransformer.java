package de.andrena.next.internal.transformer;

import de.andrena.next.internal.ContractRegistry.ContractInfo;

public final class TargetClassTransformer extends AbstractTargetClassTransformer {
	private AbstractTargetClassTransformer[] transformers = new AbstractTargetClassTransformer[] { new BeforeAndAfterTriggerTransformer() };

	protected AbstractTargetClassTransformer[] getTransformers() {
		return transformers;
	}

	@Override
	public void transform(ContractInfo contract) throws Exception {
		for (AbstractTargetClassTransformer transformer : transformers) {
			transformer.transform(contract);
		}
	}

}