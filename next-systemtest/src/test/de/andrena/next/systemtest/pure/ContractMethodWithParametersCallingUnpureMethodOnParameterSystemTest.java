package de.andrena.next.systemtest.pure;

import static de.andrena.next.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class ContractMethodWithParametersCallingUnpureMethodOnParameterSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void test() {
		new TargetClass().method(new StringBuilder());
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		public void method(StringBuilder builder) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(StringBuilder builder) {
			if (pre()) {
				builder.append("appendedString");
			}
		}
	}
}
