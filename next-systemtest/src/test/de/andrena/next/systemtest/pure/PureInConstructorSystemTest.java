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

	public static class DummyClass {
		@Pure
		public void pureMethodCallingPureConstructor() {
			new ClassWithPureConstructor();
		}

		@Pure
		public void pureMethodCallingUnpureConstructor() {
			new ClassWithUnpureConstructor();
		}
	}

	public static class ClassWithPureConstructor {
		@Pure
		public ClassWithPureConstructor() {
		}
	}

	public static class ClassWithUnpureConstructor {
	}
}
