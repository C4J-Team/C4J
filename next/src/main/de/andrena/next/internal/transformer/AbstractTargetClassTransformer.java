package de.andrena.next.internal.transformer;

import de.andrena.next.internal.ContractRegistry.ContractInfo;

public abstract class AbstractTargetClassTransformer extends ClassTransformer {
	public abstract void transform(ContractInfo contract) throws Exception;
}
