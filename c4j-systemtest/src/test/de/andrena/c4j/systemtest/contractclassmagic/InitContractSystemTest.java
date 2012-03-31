package de.andrena.c4j.systemtest.contractclassmagic;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.AllowPureAccess;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.InitializeContract;
import de.andrena.c4j.Target;

public class InitContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testInitContractCalled() {
		TargetClass target = new TargetClass();
		assertTrue(target.initContractCalled);
	}

	@ContractReference(ContractClass.class)
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

	@ContractReference(ContractClassForUnpureInitContract.class)
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
		transformerAware.expectLocalLog(Level.WARN, "Ignoring @InitializeContract method "
				+ ContractClassForInitContractWithParam.class.getName() + ".init(int)" + " as it expects parameters.");
		new TargetClassForInitContractWithParam();
	}

	@ContractReference(ContractClassForInitContractWithParam.class)
	public static class TargetClassForInitContractWithParam {
	}

	public static class ContractClassForInitContractWithParam extends TargetClassForInitContractWithParam {
		@InitializeContract
		public void init(int param) {
		}
	}

}
