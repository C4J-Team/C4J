package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.TransformationHelper.addClassAnnotation;
import javassist.CannotCompileException;
import javassist.CtClass;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.Transformed;

public class ContractClassTransformer extends AbstractContractClassTransformer {
	private AbstractContractClassTransformer[] transformers = new AbstractContractClassTransformer[] {
			// BEWARE: has to run in this exact order
			new ContractBehaviorTransformer(), new ContractExpressionTransformer(), new PureContractTransformer(),
			new TargetTransformer() };

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("transforming contract " + contractClass.getName());
		}
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
		insertUsageException(contractInfo, contractClass);
		addClassAnnotation(contractClass, ClassFilePool.INSTANCE.getClass(Transformed.class));
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
