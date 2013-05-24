package de.vksi.c4j.systemtest.config.defaultpreconditiontrueabc;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class DefaultPreConditionShouldBeUndefinedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testPreConditionUndefined() {
		new TargetClass().method(-1);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass extends SuperClass {
	}

	private static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	private static class SuperClass {
		public void method(int arg) {
		}
	}
}
