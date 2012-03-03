package de.andrena.c4j.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.Contract;
import de.andrena.next.PureTarget;

public class PureTargetSystemTest {
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

	@Contract(ContractClass.class)
	public static class TargetClass {
		private String field = "sample";

		public void pureMethod() {
		}

		public String pureMethodReadingField() {
			return field;
		}

		public void unpureMethodWritingField() {
			field = "illegal";
		}

		public void pureMethodCallingOtherPureMethod() {
			otherPureMethod();
		}

		protected void otherPureMethod() {
		}

		public void unpureMethodCallingOtherUnpureMethod() {
			otherUnpureMethod();
		}

		private void otherUnpureMethod() {
		}

		public void pureMethodCallingPureMethodInOtherClass(OtherClass other) {
			other.pureMethodInOtherClass();
		}

		public void unpureMethodCallingUnpureMethodInOtherClass(OtherClass other) {
			other.unpureMethodInOtherClass();
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		@PureTarget
		public void pureMethod() {
		}

		@Override
		@PureTarget
		public String pureMethodReadingField() {
			return null;
		}

		@Override
		@PureTarget
		public void unpureMethodWritingField() {
		}

		@Override
		@PureTarget
		public void pureMethodCallingOtherPureMethod() {
		}

		@Override
		@PureTarget
		protected void otherPureMethod() {
		}

		@Override
		@PureTarget
		public void unpureMethodCallingOtherUnpureMethod() {
		}

		@Override
		@PureTarget
		public void pureMethodCallingPureMethodInOtherClass(OtherClass other) {
		}

		@Override
		@PureTarget
		public void unpureMethodCallingUnpureMethodInOtherClass(OtherClass other) {
		}
	}

	@Contract(OtherClassContract.class)
	public static class OtherClass {
		public void pureMethodInOtherClass() {
		}

		public void unpureMethodInOtherClass() {
		}
	}

	public static class OtherClassContract extends OtherClass {
		@Override
		@PureTarget
		public void pureMethodInOtherClass() {
		}
	}
}
