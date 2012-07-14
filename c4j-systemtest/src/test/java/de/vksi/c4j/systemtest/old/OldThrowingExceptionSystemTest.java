package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldThrowingExceptionSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testOldThrowingException() {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private boolean throwException = true;
		public static boolean contractsEnabled = false;

		public void method() {
			throwException = false;
		}

		@Pure
		public int getValue() {
			if (throwException) {
				throw new RuntimeException();
			}
			return 0;
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (postCondition()) {
				// this will always evaluate to false
				if (TargetClass.contractsEnabled) {
					assert target.getValue() == old(target.getValue());
				}
			}
		}
	}
}
