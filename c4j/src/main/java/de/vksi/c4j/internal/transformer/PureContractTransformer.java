package de.vksi.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtMethod;
import de.vksi.c4j.Pure;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.Configuration.PureBehavior;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.util.PureInspector;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector = new PureInspector();
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		if (contractBehavior.hasAnnotation(Pure.class)) {
			throw new UsageError(
					"Contract method "
							+ contractBehavior.getLongName()
							+ " is marked @Pure although already being already implicitly pure. "
							+ "Consider using @PureTarget if the target method should be declared as being pure.");
		}
		if (contractBehavior instanceof CtMethod
				&& rootTransformer.getConfigurationManager().getConfiguration(contractInfo.getContractClass())
						.getPureBehaviors()
						.contains(PureBehavior.VALIDATE_PURE)) {
			pureInspector.verify((CtMethod) contractBehavior, true);
		}
	}
}
