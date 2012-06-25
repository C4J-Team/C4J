package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class AllowPureAccessSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testAllowPureAccessFromTargetClass() {
		new TargetClass().incrementValue();
	}

	@Test
	public void testAllowPureAccessFromContractClass() {
		new TargetClass().incrementValueInContract();
	}

	@ContractReference(TargetClassContract.class)
	public static class TargetClass {
		@AllowPureAccess
		private int value;

		@AllowPureAccess
		public static int staticValue;

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
			TargetClass.staticValue++;
		}
	}
}
