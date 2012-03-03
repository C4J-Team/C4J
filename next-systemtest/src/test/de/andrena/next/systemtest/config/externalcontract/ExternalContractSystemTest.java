package de.andrena.next.systemtest.config.externalcontract;

import static de.andrena.next.Condition.pre;

import org.junit.Test;

public class ExternalContractSystemTest {

	@Test(expected = AssertionError.class)
	public void testExternalContract() {
		new TargetClass().method(0);
	}

	public static class TargetClass {
		public void method(int arg) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}
}
