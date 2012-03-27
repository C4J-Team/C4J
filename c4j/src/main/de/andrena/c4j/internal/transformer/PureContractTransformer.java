package de.andrena.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtMethod;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.PureInspector;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector = new PureInspector();

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		if (contractBehavior instanceof CtMethod) {
			pureInspector.verify((CtMethod) contractBehavior, true);
		}
	}
}
