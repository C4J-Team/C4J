package de.andrena.c4j.systemtest.config.logonly;

import static de.andrena.next.Condition.pre;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Contract;
import de.andrena.next.Target;

public class LogOnlyConfigurationSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testLogOnly() {
		transformerAwareRule.expectLocalLog(Level.ERROR, "Contract Violation.");
		new TargetClass().method(0);
	}

	@Test
	public void testLogOnlyForInvariant() {
		transformerAwareRule.expectLocalLog(Level.ERROR, "Contract Violation.");
		new TargetClass().method(10);
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		protected int value;

		public void method(int value) {
			this.value = value;
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert target.value < 10 : "value < 10";
		}

		@Override
		public void method(int value) {
			if (pre()) {
				assert value > 0 : "value > 0";
			}
		}
	}
}
