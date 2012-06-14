package de.vksi.c4j.systemtest.classinvariant;

import static de.vksi.c4j.Condition.constructorCall;
import static de.vksi.c4j.Condition.old;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldInClassInvariantSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testOldInClassInvariant() {
		new TargetClass().incrementValue();
	}

	@Test(expected = AssertionError.class)
	public void testOldInClassInvariantFailing() {
		new TargetClass().decrementValue();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int value;

		public void incrementValue() {
			value++;
		}

		public void decrementValue() {
			value--;
		}
	}

	public static class ContractClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert constructorCall() || old(target.value) < target.value;
		}

	}
}
