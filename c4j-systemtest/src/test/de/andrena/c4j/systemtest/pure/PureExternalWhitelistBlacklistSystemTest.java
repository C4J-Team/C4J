package de.andrena.c4j.systemtest.pure;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.external.ExternalClass;

import de.andrena.c4j.Pure;
import de.andrena.c4j.systemtest.TransformerAwareRule;

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
				.expectLocalLog(
						Level.WARN,
						"access on unknown method "
								+ ExternalClass.class.getName()
								+ ".methodUndefinedInConfig() outside the root-packages. add it to the pure-registry in the configuration.");
		target.pureMethodCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalUndefinedThrowingNoWarning() {
		transformerAware
				.banLocalLog(
						Level.WARN,
						"access on unknown method "
								+ ExternalClass.class.getName()
								+ ".methodUndefinedInConfig() outside the root-packages. add it to the pure-registry in the configuration.");
		target.pureMethodCreatingAndCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalOnWhitelistThrowingNothing() {
		transformerAware.banLocalLog(Level.WARN,
				"access on unpure method com.external.ExternalClass.pureMethodWhitelistedInConfig() "
						+ "outside the root-packages. add it to the white- or blacklist in the configuration.");
		target.pureMethodCallingWhitelistExternal();
	}

	@Test(expected = AssertionError.class)
	public void testPureExternalOnBlacklistThrowingContractViolation() {
		target.pureMethodCallingBlacklistExternal();
	}

	@Test
	public void testPureExternalOnBlacklistThrowingNothing() {
		target.pureMethodCreatingAndCallingBlacklistExternal();
	}

	public static class TargetClass {
		private ExternalClass external = new ExternalClass();

		@Pure
		public void pureMethodCallingUndefinedExternal() {
			external.methodUndefinedInConfig();
		}

		@Pure
		public void pureMethodCreatingAndCallingUndefinedExternal() {
			new ExternalClass().methodUndefinedInConfig();
		}

		@Pure
		public void pureMethodCallingWhitelistExternal() {
			external.pureMethodWhitelistedInConfig();
		}

		@Pure
		public void pureMethodCallingBlacklistExternal() {
			external.unpureMethodBlacklistedInConfig();
		}

		@Pure
		public void pureMethodCreatingAndCallingBlacklistExternal() {
			new ExternalClass().unpureMethodBlacklistedInConfig();
		}
	}
}