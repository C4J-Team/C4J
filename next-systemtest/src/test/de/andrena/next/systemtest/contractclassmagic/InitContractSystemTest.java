package de.andrena.next.systemtest.contractclassmagic;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.AllowPureAccess;
import de.andrena.next.Contract;
import de.andrena.next.InitializeContract;
import de.andrena.next.Target;
import de.andrena.next.systemtest.TransformerAwareRule;

public class InitContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testInitContractCalled() {
		TargetClass target = new TargetClass();
		assertTrue(target.initContractCalled);
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		@AllowPureAccess
		protected boolean initContractCalled;
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@InitializeContract
		public void init() {
			target.initContractCalled = true;
		}
	}

	@Test(expected = AssertionError.class)
	public void testInitContractPure() {
		new TargetClassForUnpureInitContract();
	}

	@Contract(ContractClassForUnpureInitContract.class)
	public static class TargetClassForUnpureInitContract {
		protected int value;
	}

	public static class ContractClassForUnpureInitContract extends TargetClassForUnpureInitContract {
		@Target
		private TargetClassForUnpureInitContract target;

		@InitializeContract
		public void init() {
			target.value = 3;
		}
	}

	@Test
	public void testInitContractWithParam() {
		transformerAware
				.expectLocalLog(
						Level.WARN,
						"Ignoring @InitializeContract method de.andrena.next.systemtest.contractclassmagic.InitContractSystemTest$ContractClassForInitContractWithParam.init(int)"
								+ " as it expects parameters.");
		new TargetClassForInitContractWithParam();
	}

	@Contract(ContractClassForInitContractWithParam.class)
	public static class TargetClassForInitContractWithParam {
	}

	public static class ContractClassForInitContractWithParam extends TargetClassForInitContractWithParam {
		@InitializeContract
		public void init(int param) {
		}
	}

}
