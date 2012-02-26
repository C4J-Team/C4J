package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.Transformed;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.HelperFactory;

public class ContractClassTransformer extends AbstractContractClassTransformer {

	private AbstractContractClassTransformer[] transformers;

	public ContractClassTransformer(RootTransformer rootTransformer) {
		this.transformers = new AbstractContractClassTransformer[] {
				// BEWARE: has to run in this exact order
				new ContractBehaviorTransformer(), new PureContractTransformer(),
				new TargetTransformer(rootTransformer), new ContractExpressionTransformer(rootTransformer) };
	}

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
		HelperFactory.getTransformationHelper().addClassAnnotation(contractClass, Transformed.class);
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
