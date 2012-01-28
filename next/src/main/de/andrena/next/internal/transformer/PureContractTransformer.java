package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.editor.PureBehaviorExpressionEditor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private RootTransformer rootTransformer;

	public PureContractTransformer(RootTransformer rootTransformer) {
		this.rootTransformer = rootTransformer;
	}

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		contractBehavior.instrument(new PureBehaviorExpressionEditor(contractBehavior, rootTransformer
				.getConfiguration(), true));
	}
}
