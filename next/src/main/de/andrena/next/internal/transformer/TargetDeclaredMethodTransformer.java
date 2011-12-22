package de.andrena.next.internal.transformer;

import javassist.CtMethod;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public abstract class TargetDeclaredMethodTransformer extends AbstractTargetClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo) throws Exception {
		for (CtMethod targetMethod : contractInfo.getTargetClass().getDeclaredMethods()) {
			transform(contractInfo, targetMethod);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtMethod targetMethod) throws Exception;

}
