package de.andrena.next.internal.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public class ContractClassTransformer extends AbstractContractClassTransformer {

	private AbstractContractClassTransformer[] transformers;

	public ContractClassTransformer(ClassPool pool) {
		this.transformers = new AbstractContractClassTransformer[] { new ContractExpressionTransformer(pool) };
	}

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
