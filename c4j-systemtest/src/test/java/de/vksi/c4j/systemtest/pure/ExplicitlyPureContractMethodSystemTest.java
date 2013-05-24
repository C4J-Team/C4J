package de.vksi.c4j.systemtest.pure;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ExplicitlyPureContractMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = UsageError.class)
	public void testExplicitlyPureContractMethod() {
		new TargetClass().method(1);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method(int arg) {
		}
	}

	private static class ContractClass extends TargetClass {
		@Override
		@Pure
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}
}
