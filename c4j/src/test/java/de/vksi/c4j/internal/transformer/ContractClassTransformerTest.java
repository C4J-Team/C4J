package de.vksi.c4j.internal.transformer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Test;

import de.vksi.c4j.internal.transformer.AbstractContractClassTransformer;
import de.vksi.c4j.internal.transformer.ContractClassTransformer;

public class ContractClassTransformerTest {

	@Test
	public void testTransform() throws Exception {
		ContractClassTransformer transformer = new ContractClassTransformer();
		AbstractContractClassTransformer[] subTransformers = transformer.getTransformers();
		for (int i = 0; i < subTransformers.length; i++) {
			subTransformers[i] = mock(subTransformers[i].getClass());
		}
		CtClass contractClass = ClassPool.getDefault().get(ContractClass.class.getName());
		transformer.transform(null, contractClass);
		for (AbstractContractClassTransformer subTransformer : transformer.getTransformers()) {
			verify(subTransformer).transform(null, contractClass);
		}
	}

	public static class ContractClass {
	}
}
