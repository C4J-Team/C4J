package de.andrena.next.systemtest.precondition;

import static de.andrena.next.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class PreConditionStrengthenedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule
				.expectLogWarning("found strengthening pre-condition in"
						+ " de.andrena.next.systemtest.precondition.PreConditionStrengthenedSystemTest$ContractClass.method(int)"
						+ " which is already defined from de.andrena.next.systemtest.precondition.PreConditionStrengthenedSystemTest$SuperClassContract"
						+ " - ignoring the pre-condition");
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
