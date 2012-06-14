package de.vksi.c4j.systemtest.pure;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.external.ExternalClass;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
						"Access on unknown method "
								+ ExternalClass.class.getName()
								+ ".methodUndefinedInConfig() outside the root-packages. Add it to the pure-registry in the configuration.");
		target.pureMethodCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalUndefinedThrowingNoWarning() {
		transformerAware
				.banLocalLog(
						Level.WARN,
						"Access on unknown method "
								+ ExternalClass.class.getName()
								+ ".methodUndefinedInConfig() outside the root-packages. Add it to the pure-registry in the configuration.");
		target.pureMethodCreatingAndCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalOnWhitelistThrowingNothing() {
		transformerAware.banLocalLog(Level.WARN,
				"Access on unpure method " + ExternalClass.class.getName() + ".pureMethodWhitelistedInConfig() "
						+ "outside the root-packages. Add it to the white- or blacklist in the configuration.");
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
