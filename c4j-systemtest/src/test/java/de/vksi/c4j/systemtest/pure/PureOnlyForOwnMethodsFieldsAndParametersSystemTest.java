package de.vksi.c4j.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureOnlyForOwnMethodsFieldsAndParametersSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPure() {
		target.pureMethod("paramValue");
	}

	@Test(expected = AssertionError.class)
	public void testPureCallingUnpureOnFieldAfterSettingField() {
		target.pureMethodCallingUnpureOnFieldAfterSettingField();
	}

	@Test(expected = AssertionError.class)
	public void testPureSettingFieldTwice() {
		target.pureMethodSettingFieldTwice();
	}

	@Test(expected = AssertionError.class)
	public void testPureCallingUnpureOnField() {
		target.pureMethodCallingUnpureOnField();
	}

	@Test(expected = AssertionError.class)
	public void testPureCallingUnpureOnSelf() {
		target.pureMethodCallingUnpureOnSelf();
	}

	@Test(expected = AssertionError.class)
	public void testPureCallingUnpureOnParameter() {
		target.pureMethodCallingUnpureOnParameter(new OtherClass());
	}

	@Test(expected = AssertionError.class)
	public void testPureCallingStaticUnpure() {
		target.pureMethodCallingStaticUnpure();
	}

	@Test(expected = AssertionError.class)
	public void testPureSettingStaticField() {
		target.pureMethodSettingStaticField();
	}

	@Test(expected = NullPointerException.class)
	public void testPureCallingUnpureOnNull() {
		new NullClass().pureMethodCallingNullField();
	}

	public static class TargetClass {
		private String field = "fieldValue";
		private OtherClass otherTypeField = new OtherClass();

		@Pure
		public void pureMethod(String param) {
			StringBuilder builder = new StringBuilder();
			builder.append(field);
			builder.append(" - ");
			builder.append(param);
		}

		@Pure
		public void pureMethodCallingUnpureOnFieldAfterSettingField() {
			otherTypeField = new OtherClass();
			otherTypeField.unpureMethod();
		}

		@Pure
		public void pureMethodSettingFieldTwice() {
			otherTypeField = new OtherClass();
			otherTypeField = new OtherClass();
		}

		@Pure
		public void pureMethodCallingUnpureOnField() {
			otherTypeField.unpureMethod();
		}

		@Pure
		public void pureMethodCallingUnpureOnSelf() {
			unpureMethod();
		}

		@Pure
		public void pureMethodCallingUnpureOnParameter(OtherClass param) {
			param.unpureMethod();
		}

		@Pure
		public void pureMethodCallingStaticUnpure() {
			OtherClass.staticUnpureMethod();
		}

		@Pure
		public void pureMethodSettingStaticField() {
			OtherClass.staticField = 3;
		}

		public void unpureMethod() {
		}
	}

	public static class OtherClass {
		public static int staticField;

		public static void staticUnpureMethod() {
		}

		public void unpureMethod() {
		}
	}

	public static class NullClass {
		private OtherClass nullField;

		@Pure
		public void pureMethodCallingNullField() {
			nullField.unpureMethod();
		}
	}

}
