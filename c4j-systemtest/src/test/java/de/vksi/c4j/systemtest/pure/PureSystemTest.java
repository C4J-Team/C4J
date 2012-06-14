package de.vksi.c4j.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPureMethod() {
		target.pureMethod();
	}

	@Test
	public void testPureMethodReadingField() {
		target.pureMethodReadingField();
	}

	@Test(expected = AssertionError.class)
	public void testPureMethodWritingField() {
		target.unpureMethodWritingField();
	}

	@Test
	public void testPureMethodCallingOtherPureMethod() {
		target.pureMethodCallingOtherPureMethod();
	}

	@Test(expected = AssertionError.class)
	public void testUnpureMethodCallingOtherUnpureMethod() {
		target.unpureMethodCallingOtherUnpureMethod();
	}

	@Test
	public void testPureMethodCallingPureMethodInOtherClass() {
		target.pureMethodCallingPureMethodInOtherClass(new OtherClass());
	}

	@Test(expected = AssertionError.class)
	public void testUnpureMethodCallingUnpureMethodInOtherClass() {
		target.unpureMethodCallingUnpureMethodInOtherClass(new OtherClass());
	}

	public static class TargetClass {
		private String field = "sample";

		@Pure
		public void pureMethod() {
		}

		@Pure
		public String pureMethodReadingField() {
			return field;
		}

		@Pure
		public void unpureMethodWritingField() {
			field = "illegal";
		}

		@Pure
		public void pureMethodCallingOtherPureMethod() {
			otherPureMethod();
		}

		@Pure
		private void otherPureMethod() {
		}

		@Pure
		public void unpureMethodCallingOtherUnpureMethod() {
			otherUnpureMethod();
		}

		private void otherUnpureMethod() {
		}

		@Pure
		public void pureMethodCallingPureMethodInOtherClass(OtherClass other) {
			other.pureMethodInOtherClass();
		}

		@Pure
		public void unpureMethodCallingUnpureMethodInOtherClass(OtherClass other) {
			other.unpureMethodInOtherClass();
		}
	}

	public static class OtherClass {
		@Pure
		public void pureMethodInOtherClass() {
		}

		public void unpureMethodInOtherClass() {
		}
	}
}
