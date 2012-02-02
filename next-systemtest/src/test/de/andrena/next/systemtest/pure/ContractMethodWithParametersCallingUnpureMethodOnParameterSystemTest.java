package de.andrena.next.systemtest.pure;

import static de.andrena.next.Condition.pre;

import org.junit.Test;

import de.andrena.next.Contract;

public class ContractMethodWithParametersCallingUnpureMethodOnParameterSystemTest {

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
