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
		new TargetClass().method(new OtherClass());
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		public void method(OtherClass other) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(OtherClass other) {
			if (pre()) {
				other.unpureMethod();
			}
		}
	}

	public static class OtherClass {
		public void unpureMethod() {
		}
	}
}
