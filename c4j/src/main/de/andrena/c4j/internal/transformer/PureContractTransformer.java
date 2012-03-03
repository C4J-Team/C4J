package de.andrena.c4j.internal.transformer;

import javassist.CtBehavior;
import de.andrena.c4j.internal.util.PureInspector;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector = new PureInspector();

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		pureInspector.verify(contractBehavior, true);
	}
}
