package de.andrena.next.systemtest.pure;

import org.junit.Test;

import de.andrena.next.AllowPureAccess;
import de.andrena.next.Contract;
import de.andrena.next.Pure;

public class AllowPureAccessSystemTest {

	@Test
	public void testAllowPureAccessFromTargetClass() {
		new TargetClass().incrementValue();
	}

	@Test
	public void testAllowPureAccessFromContractClass() {
		new TargetClass().incrementValueInContract();
	}

	@Contract(TargetClassContract.class)
	public static class TargetClass {
		@AllowPureAccess
		protected int value;

		@Pure
		public void incrementValue() {
			value++;
		}

		@Pure
		public void incrementValueInContract() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Override
		public void incrementValueInContract() {
			value++;
		}
	}
}
