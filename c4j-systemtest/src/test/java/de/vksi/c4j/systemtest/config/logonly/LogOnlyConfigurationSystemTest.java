package de.vksi.c4j.systemtest.config.logonly;

import static de.vksi.c4j.Condition.preCondition;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class LogOnlyConfigurationSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testLogOnly() {
		transformerAwareRule.expectLocalLog(Level.ERROR, "Contract Violation in pre-condition.");
		new TargetClass().method(0);
	}

	@Test
	public void testLogOnlyForInvariant() {
		transformerAwareRule.expectLocalLog(Level.ERROR, "Contract Violation in class-invariant.");
		new TargetClass().method(10);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		protected int value;

		public void method(int value) {
			this.value = value;
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert target.value < 10 : "value < 10";
		}

		@Override
		public void method(int value) {
			if (preCondition()) {
				assert value > 0 : "value > 0";
			}
		}
	}
}
