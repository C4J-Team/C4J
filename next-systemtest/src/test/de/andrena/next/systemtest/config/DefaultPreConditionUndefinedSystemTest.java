package de.andrena.next.systemtest.config;

import static de.andrena.next.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class DefaultPreConditionUndefinedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		new TargetClass().method(1);
	}

	@Contract(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}

	public static class SuperClass {
		public void method(int arg) {
		}
	}

}
