package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractClassPrivateIsMadeAccessibleSystemTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void privateContractClass_IsMadeAccessible() {
		new TargetClass().method(1);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method(int arg) {
		}
	}

	private static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}
}
