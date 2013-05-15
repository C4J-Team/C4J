package de.vksi.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtMethod;
import de.vksi.c4j.Pure;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.editor.PureInspector;

public class PureContractTransformer extends ContractDeclaredBehaviorTransformer {

	private PureInspector pureInspector = new PureInspector();

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		if (contractBehavior.hasAnnotation(Pure.class)) {
			contractInfo.addError(new UsageError("Contract method " + contractBehavior.getLongName()
					+ " is marked @Pure although already being already implicitly pure. "
					+ "Consider using @PureTarget if the target method should be declared as being pure."));
		}
		if (contractBehavior instanceof CtMethod
				&& XmlConfigurationManager.INSTANCE.getConfiguration(contractInfo.getContractClass()).isPureValidate()) {
			pureInspector.verify((CtMethod) contractBehavior, true);
		}
	}
}
