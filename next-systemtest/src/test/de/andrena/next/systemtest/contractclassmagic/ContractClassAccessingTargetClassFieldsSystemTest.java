package de.andrena.next.systemtest.contractclassmagic;

import static de.andrena.next.Condition.post;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.Target;
import de.andrena.next.systemtest.TransformerAwareRule;

public class ContractClassAccessingTargetClassFieldsSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	// TODO
	@Test
	public void testContractClassAccessingTargetClassField() {
		new TargetClass().method(3, 3);
	}

	@Contract(ContractClass.class)
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
