package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class InitContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testInitContractNotCalledWithoutCondition() {
		TargetClass target = new TargetClass();
		assertFalse(target.initContractCalled);
	}

	@Test
	public void testInitContractCalled() {
		TargetClass target = new TargetClass();
		target.method();
		assertTrue(target.initContractCalled);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		@AllowPureAccess
		protected boolean initContractCalled;

		public void method() {
		}
	}

	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		public ContractClass() {
			target.initContractCalled = true;
		}

		@Override
		public void method() {
			if (preCondition()) {
			}
		}
	}
}
