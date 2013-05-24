package de.vksi.c4j.systemtest.config.contractsdirectory;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Contract;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractsDirectorySystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testContractsDirectory() {
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

}
