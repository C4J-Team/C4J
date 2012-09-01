package de.vksi.c4j.systemtest.pure;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.external.ExternalClass;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureStaticExternalWhitelistBlacklistSystemTest {
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
				.expectGlobalLog(
						Level.WARN,
						"Access on unpure object, method "
								+ ExternalClass.class.getName()
								+ ".staticMethodUndefinedInConfig() outside the root-packages. Add it to the pure-registry in the configuration.");
		target.pureMethodCallingUndefinedExternal();
	}

	@Test
	public void testPureExternalOnWhitelistThrowingNothing() {
		transformerAware.banGlobalLog(Level.WARN, "Access on unpure method " + ExternalClass.class.getName()
				+ ".pureStaticMethodWhitelistedInConfig() "
				+ "outside the root-packages. Add it to the white- or blacklist in the configuration.");
		target.pureMethodCallingWhitelistExternal();
	}

	@Test(expected = AssertionError.class)
	public void testPureExternalOnBlacklistThrowingContractViolation() {
		target.pureMethodCallingBlacklistExternal();
	}

	public static class TargetClass {
		@Pure
		public void pureMethodCallingUndefinedExternal() {
			ExternalClass.staticMethodUndefinedInConfig();
		}

		@Pure
		public void pureMethodCallingWhitelistExternal() {
			ExternalClass.pureStaticMethodWhitelistedInConfig();
		}

		@Pure
		public void pureMethodCallingBlacklistExternal() {
			ExternalClass.unpureStaticMethodBlacklistedInConfig();
		}

	}
}
