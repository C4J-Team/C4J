package de.vksi.c4j.systemtest.config.assertionerroronly;

import static de.vksi.c4j.Condition.preCondition;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
			if (preCondition()) {
				assert value > 0;
			}
		}
	}
}
