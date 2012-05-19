package de.andrena.c4j.systemtest.config.contractsdirectory;

import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.Contract;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ContractsDirectorySystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testContractsDirectory() {
		new TargetClass().method(0);
	}

	public static class TargetClass {
		public void method(int arg) {
		}
	}

	@Contract
	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}

}
