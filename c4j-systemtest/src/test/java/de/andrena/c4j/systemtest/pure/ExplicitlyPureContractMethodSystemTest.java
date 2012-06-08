package de.andrena.c4j.systemtest.pure;

import static de.andrena.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.UsageError;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ExplicitlyPureContractMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = UsageError.class)
	public void testExplicitlyPureContractMethod() {
		new TargetClass().method(1);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(int arg) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		@Pure
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}
}
