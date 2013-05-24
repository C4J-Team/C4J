package de.vksi.c4j.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.external.ExternalClass;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureStaticFieldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test(expected = AssertionError.class)
	public void testPureExternalStaticFieldThrowingContractViolation() {
		target.pureMethodWritingExternalStaticField();
	}

	@Test(expected = AssertionError.class)
	public void testPureWritingStaticFieldThrowingContractViolation() {
		target.pureMethodWritingStaticField();
	}

	@Test
	public void testPureWritingStaticFieldAllowed() {
		target.pureMethodWritingStaticFieldAllowed();
	}

	@SuppressWarnings("unused")
	private static class TargetClass {
		protected static int STATIC_FIELD;

		@AllowPureAccess
		protected static int STATIC_FIELD_ALLOWED;

		@Pure
		public void pureMethodWritingExternalStaticField() {
			ExternalClass.STATIC_FIELD = 3;
		}

		@Pure
		public void pureMethodWritingStaticField() {
			STATIC_FIELD = 3;
		}

		@Pure
		public void pureMethodWritingStaticFieldAllowed() {
			STATIC_FIELD_ALLOWED = 3;
		}

	}
}
