package de.andrena.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Pure;

public class PureChangingFieldInNestedMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testChangingFieldInNestedConstructor() {
		new TargetClass().method();
	}

	public static class TargetClass {
		public int value;

		@Pure
		public void method() {
			new OtherClass().otherMethod(this);
		}
	}

	public static class OtherClass {
		public void otherMethod(TargetClass target) {
			target.value = 3;
		}

	}
}
