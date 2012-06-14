package de.vksi.c4j.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtClass;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.Transformed;
import de.vksi.c4j.internal.util.TransformationHelper;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

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
		try {
			for (AbstractContractClassTransformer transformer : transformers) {
				transformer.transform(contractInfo, contractClass);
			}
		} catch (UsageError e) {
			insertUsageException(e, contractClass);
		}
		transformationHelper.addClassAnnotation(contractClass,
				RootTransformer.INSTANCE.getPool().get(Transformed.class.getName()));
	}

	private void insertUsageException(UsageError exception, CtClass contractClass) throws CannotCompileException {
		exception.insertThrowExp(contractClass.makeClassInitializer());
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
