package de.vksi.c4j.systemtest.cleanup;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnpureCacheSideEffectSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testSideEffect() {
		new TargetClass().pureMethod();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		private OtherClass other = new OtherClass();

		@Pure
		public void pureMethod() {
			unpureMethod();
		}

		protected void unpureMethod() {
			other.unpureMethod();
		}
	}

	private static class OtherClass {
		public void unpureMethod() {
		}
	}

	private static class ContractClass extends TargetClass {
		@Override
		protected void unpureMethod() {
			if (preCondition()) {
			}
		}
	}
}
