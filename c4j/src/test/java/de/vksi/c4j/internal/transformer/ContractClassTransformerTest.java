package de.vksi.c4j.internal.transformer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Test;

import de.vksi.c4j.internal.util.ContractRegistry;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

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
		ContractInfo contractInfo = new ContractRegistry().registerContract(targetClass, contractClass);
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
