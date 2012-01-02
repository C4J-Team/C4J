package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public abstract class TargetDeclaredBehaviorTransformer extends AbstractTargetClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo) throws Exception {
		for (CtBehavior targetBehavior : contractInfo.getTargetClass().getDeclaredBehaviors()) {
			transform(contractInfo, targetBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior targetBehavior) throws Exception;

}
