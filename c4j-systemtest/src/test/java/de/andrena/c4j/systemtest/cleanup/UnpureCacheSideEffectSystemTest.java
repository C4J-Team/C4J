package de.andrena.c4j.systemtest.cleanup;

import static de.andrena.c4j.Condition.noneIdentifiedYet;
import static de.andrena.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class UnpureCacheSideEffectSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testSideEffect() {
		new TargetClass().pureMethod();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private OtherClass other = new OtherClass();

		@Pure
		public void pureMethod() {
			unpureMethod();
		}

		protected void unpureMethod() {
			other.unpureMethod();
		}
	}

	public static class OtherClass {
		public void unpureMethod() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		protected void unpureMethod() {
			if (preCondition()) {
				noneIdentifiedYet();
			}
		}
	}
}
