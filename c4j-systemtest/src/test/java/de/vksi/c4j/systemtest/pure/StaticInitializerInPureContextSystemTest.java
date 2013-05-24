package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class StaticInitializerInPureContextSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void enumReferenceSettingOwnFields() throws Exception {
		new DummyClass().methodReferencingEnum();
	}

	@Test
	public void staticInitializerSettingOwnFields() throws Exception {
		new DummyClass().methodCausingStaticInitializerCall();
	}

	@Test(expected = AssertionError.class)
	public void staticInitializerSettingExternalFields() throws Exception {
		new DummyClass().methodCausingStaticInitializerCallInitializingExternalFields();
	}

	private static class DummyClass {
		@Pure
		public void methodReferencingEnum() {
			doNothingWithEnumValue(SampleEnum.ENUM_VALUE);
		}

		@Pure
		public void methodCausingStaticInitializerCallInitializingExternalFields() {
			doNothingWithIntValue(StaticClassInitializingExternalFields.ownValue);
		}

		@Pure
		public void methodCausingStaticInitializerCall() {
			doNothingWithIntValue(StaticClass.value);
		}

		@Pure
		private void doNothingWithIntValue(int value) {
		}

		@Pure
		private void doNothingWithEnumValue(SampleEnum enumValue) {
		}
	}

	private static class StaticClassInitializingExternalFields {
		public static int ownValue;

		static {
			ownValue = 2;
			StaticClass.value = 5;
		}
	}

	private static class StaticClass {
		public static int value;

		static {
			value = 3;
		}
	}

	public static enum SampleEnum {
		ENUM_VALUE;
	}

}
