package de.andrena.next.systemtest.contractclassmagic;

import org.junit.Test;

import de.andrena.next.Contract;

public class ContractClassAccessingTargetClassFieldsSystemTest {

	@Test
	public void testContractClassAccessingTargetClassField() {
		new TargetClass().method();
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		protected int value;

		public void method() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method() {
			value++;
		}
	}
}
