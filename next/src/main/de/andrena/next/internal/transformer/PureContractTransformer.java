package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.PureInspector;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector;

	public PureContractTransformer(RootTransformer rootTransformer) {
		this.pureInspector = new PureInspector(rootTransformer);
	}

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		pureInspector.verify(contractBehavior, true);
	}
}
