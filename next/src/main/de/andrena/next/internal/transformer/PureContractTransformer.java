package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.editor.PureBehaviorExpressionEditor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.PureInspector;
import de.andrena.next.internal.util.PureInspectorProvider;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer implements PureInspectorProvider {

	private RootTransformer rootTransformer;
	private PureInspector pureInspector = new PureInspector();

	public PureContractTransformer(RootTransformer rootTransformer) {
		this.rootTransformer = rootTransformer;
	}

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		contractBehavior.instrument(new PureBehaviorExpressionEditor(contractBehavior, rootTransformer, this, true));
	}

	@Override
	public PureInspector getPureInspector() {
		return pureInspector;
	}
}
