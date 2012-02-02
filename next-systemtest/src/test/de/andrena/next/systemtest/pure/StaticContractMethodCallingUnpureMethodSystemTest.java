package de.andrena.next.systemtest.pure;

import org.junit.Test;

import de.andrena.next.Contract;

public class StaticContractMethodCallingUnpureMethodSystemTest {

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
