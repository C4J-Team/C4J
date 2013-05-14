package de.vksi.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtMethod;
import de.vksi.c4j.Pure;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.PureInspector;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector = new PureInspector();
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		if (contractBehavior.hasAnnotation(Pure.class)) {
			contractInfo.addError(new UsageError("Contract method " + contractBehavior.getLongName()
					+ " is marked @Pure although already being already implicitly pure. "
					+ "Consider using @PureTarget if the target method should be declared as being pure."));
		}
		if (contractBehavior instanceof CtMethod
				&& rootTransformer.getXmlConfiguration().getConfiguration(contractInfo.getContractClass())
						.isPureValidate()) {
			pureInspector.verify((CtMethod) contractBehavior, true);
		}
	}
}
