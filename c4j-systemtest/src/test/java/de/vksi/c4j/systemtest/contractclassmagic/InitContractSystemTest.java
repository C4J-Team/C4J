package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.InitializeContract;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class InitContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testInitContractCalledWithoutCondition() {
		TargetClass target = new TargetClass();
		assertTrue(target.initContractCalled);
	}

	@Test
	public void testInitContractCalled() {
		TargetClass target = new TargetClass();
		target.method();
		assertTrue(target.initContractCalled);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		@AllowPureAccess
		protected boolean initContractCalled;

		public void method() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@InitializeContract
		public void init() {
			target.initContractCalled = true;
		}

		@Override
		public void method() {
			if (preCondition()) {
			}
		}
	}

	@Test(expected = AssertionError.class)
	public void testInitContractPure() {
		new TargetClassForUnpureInitContract().method();
	}

	@ContractReference(ContractClassForUnpureInitContract.class)
	public static class TargetClassForUnpureInitContract {
		protected int value;

		public void method() {
		}
	}

	public static class ContractClassForUnpureInitContract extends TargetClassForUnpureInitContract {
		@Target
		private TargetClassForUnpureInitContract target;

		@InitializeContract
		public void init() {
			target.value = 3;
		}

		@Override
		public void method() {
			if (preCondition()) {
			}
		}
	}

	@Test
	public void testInitContractWithParam() {
		new TargetClassForInitContractWithParam();
		transformerAware.expectGlobalLog(Level.WARN, "Ignoring @InitializeContract method "
				+ ContractClassForInitContractWithParam.class.getName() + ".init(int)" + " as it expects parameters.");
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
