package de.andrena.next.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class StaticContractMethodCallingUnpureMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void test() {
		new TargetClass().method();
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		public void method() {
		}
	}

	public static class ContractClass extends TargetClass {
		protected TargetClass target;

		static {
			new OtherClass().unpureMethod();
		}

	}

	public static class OtherClass {

		public void unpureMethod() {
		}
	}
}
