package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.post;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractClassAccessingTargetClassFieldsSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	// TODO
	@Test
	public void testContractClassAccessingTargetClassField() {
		new TargetClass().method(3, 3);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int value;

		public void method(int value, int param) {
			this.value = value;
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method(int value, int param) {
			if (post()) {
				assert target.value >= param;
			}
		}
	}
}
