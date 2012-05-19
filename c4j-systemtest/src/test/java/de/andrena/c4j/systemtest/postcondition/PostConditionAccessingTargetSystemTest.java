package de.andrena.c4j.systemtest.postcondition;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class PostConditionAccessingTargetSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPostConditionAccessingTarget() {
		TargetClass target = new TargetClass();
		target.method(3, 4);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private int z;

		public int method(int x, int y) {
			return z = x + y;
		}

		@Pure
		public int getZ() {
			return z;
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public int method(int x, int y) {
			if (post()) {
				// necessary for reproduction
				target.getZ();
				assert result(Integer.class).intValue() == x + y;
			}
			return ignored();
		}
	}
}
