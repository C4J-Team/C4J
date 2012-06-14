package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractClassDefiningOwnConstructorSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractClassDefiningOwnConstructor() {
		new TargetClass(5).getValue();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private int value;

		public TargetClass(int value) {
			this.value = value;
		}

		@Pure
		public int getValue() {
			return value;
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass(String value) {
			super(3);
		}

		@Override
		public int getValue() {
			if (postCondition()) {
				int result = (Integer) result();
				assert result > 0;
			}
			return (Integer) ignored();
		}
	}
}
