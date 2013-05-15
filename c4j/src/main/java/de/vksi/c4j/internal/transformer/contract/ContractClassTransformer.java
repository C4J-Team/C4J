package de.vksi.c4j.internal.transformer.contract;

import static de.vksi.c4j.internal.transformer.util.TransformationHelper.addClassAnnotation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.compiler.ConstructorExp;
import de.vksi.c4j.internal.compiler.ThrowExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.Transformed;

public class ContractClassTransformer extends AbstractContractClassTransformer {
	private static final Logger LOGGER = Logger.getLogger(ContractClassTransformer.class);

	private AbstractContractClassTransformer[] transformers = new AbstractContractClassTransformer[] {
			// BEWARE: has to run in this exact order
			new ContractBehaviorTransformer(), new ContractExpressionTransformer(), new PureContractTransformer(),
			new TargetTransformer() };

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("transforming contract " + contractClass.getName());
		}
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
		insertUsageException(contractInfo, contractClass);
		addClassAnnotation(contractClass, ClassFilePool.INSTANCE.getClass(Transformed.class));
	}

	private void insertUsageException(ContractInfo contractInfo, CtClass contractClass) throws CannotCompileException {
		if (!contractInfo.getErrors().isEmpty()) {
			insertThrowUsageError(contractClass.makeClassInitializer(), contractInfo.getErrors().get(0).getMessage());
		}
	}

	private void insertThrowUsageError(CtBehavior behavior, String message) throws CannotCompileException {
		new ThrowExp(new ConstructorExp(UsageError.class, new ValueExp(message))).insertBefore(behavior);
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
