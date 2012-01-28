package de.andrena.next.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Pure;
import de.andrena.next.systemtest.TransformerAwareRule;

public class PureInConstructorSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testCallingPureConstructor() {
		new DummyClass().pureMethodCallingPureConstructor();
	}

	@Test(expected = AssertionError.class)
	public void testCallingUnpureConstructor() {
		new DummyClass().pureMethodCallingUnpureConstructor();
	}

	public static class ExternalClass {
		public static int VALUE;
	}

	public static class DummyClass {
		@Pure
		public void pureMethodCallingPureConstructor() {
			new ClassWithPureConstructor(3);
		}

		@Pure
		public void pureMethodCallingUnpureConstructor() {
			new ClassWithUnpureConstructor(3);
		}
	}

	public static class ClassWithPureConstructor {
		protected int value;

		@Pure
		public ClassWithPureConstructor(int value) {
			this.value = value;
			setValue(value);
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class ClassWithUnpureConstructor {
		public ClassWithUnpureConstructor(int value) {
			ExternalClass.VALUE = 1;
		}
	}

	@Test(expected = AssertionError.class)
	public void testPureConstructorCallingUnpureConstructor() {
		new ClassWithPureConstructorCallingUnpureConstructor();
	}

	public static class ClassWithPureConstructorCallingUnpureConstructor {
		@Pure
		public ClassWithPureConstructorCallingUnpureConstructor() {
			this(3);
		}

		public ClassWithPureConstructorCallingUnpureConstructor(int value) {
			ExternalClass.VALUE = 1;
		}
	}

	@Test(expected = AssertionError.class)
	public void testPureConstructorCallingUnpureSuperConstructor() {
		new ClassWithPureConstructorCallingUnpureSuperConstructor();
	}

	public static class ClassWithPureConstructorCallingUnpureSuperConstructor extends ClassWithUnpureSuperConstructor {
		@Pure
		public ClassWithPureConstructorCallingUnpureSuperConstructor() {
			super(3);
		}
	}

	public static class ClassWithUnpureSuperConstructor {
		public ClassWithUnpureSuperConstructor(int value) {
			ExternalClass.VALUE = 1;
		}
	}
}
