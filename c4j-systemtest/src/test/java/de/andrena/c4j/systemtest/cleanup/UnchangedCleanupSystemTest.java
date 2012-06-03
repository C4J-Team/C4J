package de.andrena.c4j.systemtest.cleanup;

import static de.andrena.c4j.Condition.postCondition;
import static de.andrena.c4j.Condition.preCondition;
import static de.andrena.c4j.Condition.unchanged;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.internal.evaluator.PureEvaluator;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class UnchangedCleanupSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testUnchangedCleanup() {
		new TargetClass().method(1);
		assertTrue(PureEvaluator.isUnpureCacheEmpty());
	}

	@Test
	public void testUnchangedCleanupAfterPreConditionFails() {
		try {
			new TargetClass().method(0);
		} catch (AssertionError e) {
			// expected
		}
		assertTrue(PureEvaluator.isUnpureCacheEmpty());
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(int arg) {

		}

		@Pure
		public String getName() {
			return "constantString";
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
			if (postCondition()) {
				assert unchanged(target.getName());
			}
		}
	}
}
