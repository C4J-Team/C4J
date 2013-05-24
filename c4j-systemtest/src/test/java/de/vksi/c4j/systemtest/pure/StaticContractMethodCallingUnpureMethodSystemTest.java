package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class StaticContractMethodCallingUnpureMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void test() {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method() {
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass extends TargetClass {
		protected TargetClass target;

		static {
			new OtherClass().unpureMethod();
		}

	}

	private static class OtherClass {

		public void unpureMethod() {
		}
	}
}
