package de.andrena.next.systemtest.config.invalidpreconditionbehaviorerror;

import static de.andrena.next.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class InvalidPreConditionBehaviorErrorSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule
				.expectTransformationException("found strengthening pre-condition in"
						+ " de.andrena.next.systemtest.config.invalidpreconditionbehaviorerror.InvalidPreConditionBehaviorErrorSystemTest$ContractClass.method(int)"
						+ " which is already defined from"
						+ " de.andrena.next.systemtest.config.invalidpreconditionbehaviorerror.InvalidPreConditionBehaviorErrorSystemTest$SuperClassContract");
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

	@Contract(SuperClassContract.class)
	public static class SuperClass {
		public void method(int arg) {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > -1;
			}
		}
	}
}
