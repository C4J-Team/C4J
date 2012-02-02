package de.andrena.next.systemtest.contractclassmagic;

import org.junit.Test;

import de.andrena.next.Contract;

public class PureCheckForClassInitializerSystemTest {

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
