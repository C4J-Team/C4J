package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureCallingStaticUnpureInNestedMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testCallingStaticUnpureInNestedConstructor() {
		new TargetClass().methodCallingStaticUnpureInNestedConstructor();
	}

	@Test(expected = AssertionError.class)
	public void testCallingStaticUnpureInNestedMethodModifyingOwnStaticField() {
		new TargetClass().methodCallingStaticUnpureInNestedMethodModifyingOwnStaticField();
	}

	@Test
	public void testCallingStaticUnpureInNestedMethodModifyingAccessibleField() {
		new TargetClass().methodCallingStaticUnpureInNestedMethodModifyingAccessibleStaticField();
	}

	public static class TargetClass {
		public static int value;

		@Pure
		public void methodCallingStaticUnpureInNestedConstructor() {
			new UnpureClass();
		}

		@Pure
		public void methodCallingStaticUnpureInNestedMethodModifyingOwnStaticField() {
			new OtherClass().methodModifyingOwnStaticField();
		}

		@Pure
		public void methodCallingStaticUnpureInNestedMethodModifyingAccessibleStaticField() {
			new AllowingClass().methodModifyingAccessibleStaticField();
		}
	}

	public static class UnpureClass {
		public UnpureClass() {
			TargetClass.value = 3;
		}
	}

	public static class OtherClass {
		protected static int value;

		public void methodModifyingOwnStaticField() {
			OtherClass.value = 3;
		}
	}

	public static class AllowingClass {
		@AllowPureAccess
		protected static int value;

		public void methodModifyingAccessibleStaticField() {
			AllowingClass.value = 3;
		}
	}
}
