package de.andrena.c4j.systemtest.config.assertionerroronly;

import static de.andrena.c4j.Condition.pre;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;

public class AssertionErrorOnlyConfigurationSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testLogOnly() {
		transformerAwareRule.banLocalLog(Level.ERROR, "Contract Violation.");
		try {
			new TargetClass().method(0);
		} catch (AssertionError e) {
		}
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(int value) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int value) {
			if (pre()) {
				assert value > 0;
			}
		}
	}
}
