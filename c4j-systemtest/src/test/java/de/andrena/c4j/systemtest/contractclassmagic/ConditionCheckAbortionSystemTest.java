package de.andrena.c4j.systemtest.contractclassmagic;

import static de.andrena.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ConditionCheckAbortionSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testConditionCheckAbortionAfterFirstPostCondition() {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("method 2 (post-condition)");
		new TargetClass().method1();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method1() {
			method2();
		}

		public void method2() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method1() {
			if (postCondition()) {
				assert false : "method 1";
			}
		}

		@Override
		public void method2() {
			if (postCondition()) {
				assert false : "method 2";
			}
		}
	}
}
