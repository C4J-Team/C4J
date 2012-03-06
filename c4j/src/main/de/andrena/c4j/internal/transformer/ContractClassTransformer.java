package de.andrena.c4j.internal.transformer;

import javassist.CtClass;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.Transformed;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.TransformationHelper;

public class ContractClassTransformer extends AbstractContractClassTransformer {
	private AbstractContractClassTransformer[] transformers = new AbstractContractClassTransformer[] {
			// BEWARE: has to run in this exact order
			new ContractBehaviorTransformer(), new PureContractTransformer(),
			new ContractExpressionTransformer(), new TargetTransformer() };
	private TransformationHelper transformationHelper = new TransformationHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
		transformationHelper.addClassAnnotation(contractClass,
				RootTransformer.INSTANCE.getPool().get(Transformed.class.getName()));
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
