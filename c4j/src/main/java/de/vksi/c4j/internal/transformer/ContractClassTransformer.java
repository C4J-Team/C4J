package de.vksi.c4j.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtClass;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.Transformed;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.TransformationHelper;

public class ContractClassTransformer extends AbstractContractClassTransformer {
	private AbstractContractClassTransformer[] transformers = new AbstractContractClassTransformer[] {
			// BEWARE: has to run in this exact order
			new ContractBehaviorTransformer(), new ContractExpressionTransformer(), new PureContractTransformer(),
			new TargetTransformer() };
	private TransformationHelper transformationHelper = new TransformationHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("transforming contract " + contractClass.getName());
		}
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
		insertUsageException(contractInfo, contractClass);
		transformationHelper.addClassAnnotation(contractClass, RootTransformer.INSTANCE.getPool().get(
				Transformed.class.getName()));
	}

	private void insertUsageException(ContractInfo contractInfo, CtClass contractClass) throws CannotCompileException {
		if (!contractInfo.getErrors().isEmpty()) {
			contractInfo.getErrors().get(0).insertThrowExp(contractClass.makeClassInitializer());
		}
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
