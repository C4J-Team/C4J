package de.andrena.next.internal.transformer;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ContractClassTransformerTest {

	@Test
	public void testTransform() throws Exception {
		ContractClassTransformer transformer = new ContractClassTransformer(null);
		AbstractContractClassTransformer[] subTransformers = transformer.getTransformers();
		for (int i = 0; i < subTransformers.length; i++) {
			subTransformers[i] = mock(subTransformers[i].getClass());
		}
		transformer.transform(null, null);
		for (AbstractContractClassTransformer subTransformer : transformer.getTransformers()) {
			verify(subTransformer).transform(null, null);
		}
	}
}
