package de.vksi.c4j.systemtest.config.externalcontract;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Contract;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ExternalContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testExternalContract() {
		new TargetClass().method(0);
	}

	private static class TargetClass {
		public void method(int arg) {
		}
	}

	@SuppressWarnings("unused")
	@Contract
	private static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@Test
	public void testLocalContractPreferred() {
		new TargetClassWithLocalAndExternalContract().method(1);
	}

	@Test(expected = AssertionError.class)
	public void testLocalContractPreferredFailing() {
		new TargetClassWithLocalAndExternalContract().method(0);
	}

	@ContractReference(LocalContract.class)
	private static class TargetClassWithLocalAndExternalContract {
		public void method(int arg) {
		}
	}

	private static class LocalContract extends TargetClassWithLocalAndExternalContract {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@SuppressWarnings("unused")
	private static class ExternalContract extends TargetClassWithLocalAndExternalContract {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 1;
			}
		}
	}
}
