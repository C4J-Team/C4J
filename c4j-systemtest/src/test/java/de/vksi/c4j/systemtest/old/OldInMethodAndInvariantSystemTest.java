package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldInMethodAndInvariantSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testOldInMethodAndInvariant() {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method() {
		}

		@Pure
		public int getZero() {
			return 0;
		}

		@Pure
		public int getOne() {
			return 1;
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert old(target.getOne()) == 1;
		}

		@Override
		public void method() {
			if (postCondition()) {
				assert old(target.getZero()) == 0;
			}
		}

	}
}
