package de.andrena.c4j.systemtest.contractclassmagic;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.systemtest.TransformerAwareRule;

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
			if (post()) {
				assert result(Integer.class).intValue() > 0;
			}
			return ignored();
		}
	}
}
