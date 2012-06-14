package de.vksi.c4j.systemtest.pure;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractMethodWithParametersCallingUnpureMethodOnParameterSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void test() {
		new TargetClass().method(new OtherClass());
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(OtherClass other) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(OtherClass other) {
			if (preCondition()) {
				other.unpureMethod();
			}
		}
	}

	public static class OtherClass {
		public void unpureMethod() {
		}
	}
}
