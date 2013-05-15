package de.vksi.c4j.internal.transformer.contract;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Test;

import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;

public class ContractClassTransformerTest {

	@Test
	public void testTransform() throws Exception {
		ContractClassTransformer transformer = new ContractClassTransformer();
		AbstractContractClassTransformer[] subTransformers = transformer.getTransformers();
		for (int i = 0; i < subTransformers.length; i++) {
			subTransformers[i] = mock(subTransformers[i].getClass());
		}
		CtClass targetClass = ClassPool.getDefault().get(TargetClass.class.getName());
		CtClass contractClass = ClassPool.getDefault().get(ContractClass.class.getName());
		contractClass.defrost();
		ContractInfo contractInfo = ContractRegistry.INSTANCE.registerContract(targetClass, contractClass);
		transformer.transform(contractInfo, contractClass);
		for (AbstractContractClassTransformer subTransformer : transformer.getTransformers()) {
			verify(subTransformer).transform(contractInfo, contractClass);
		}
	}

	public static class TargetClass {
	}

	public static class ContractClass {
	}
}
