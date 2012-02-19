package de.andrena.next.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.external.ExternalClass;

import de.andrena.next.Pure;
import de.andrena.next.systemtest.TransformerAwareRule;

public class PureExternalWhitelistBlacklistSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPureExternalUndefinedThrowingWarning() {
		transformerAware
				.expectLogWarning("access on unpure method com.external.ExternalClass.unpureMethodUndefinedInConfig() "
						+ "outside the root-packages. add it to the white- or blacklist in the configuration.");
		target.pureMethodCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalOnWhitelistThrowingNothing() {
		target.pureMethodCallingWhitelistExternal();
	}

	public static class TargetClass {
		private ExternalClass external = new ExternalClass();

		@Pure
		public void pureMethodCallingUndefinedExternal() {
			external.unpureMethodUndefinedInConfig();
		}

		@Pure
		public void pureMethodCallingWhitelistExternal() {
			external.unpureMethodWhitelistedInConfig();
		}
	}
}
