package de.andrena.c4j.systemtest.config.invalidpreconditionbehaviorerror;

import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;

public class InvalidPreConditionBehaviorErrorSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule.expectTransformationException("found strengthening pre-condition in "
				+ ContractClass.class.getName() + ".method(int)" + " which is already defined from" + " "
				+ SuperClassContract.class.getName());
		new TargetClass().method(0);
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
