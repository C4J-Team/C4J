package de.vksi.c4j.outsideacceptancetest;

import org.junit.Test;

import de.vksi.c4j.outsidesystemtest.RuleEnforcingTestBase;

public class RuleEnforcingTest extends RuleEnforcingTestBase {
	private static final String ACCEPTANCETEST_PACKAGE_NAME = "de.vksi.c4j.acceptancetest";

	@Test
	public void allTestsUseRule() throws Exception {
		enforceRuleInPackage(ACCEPTANCETEST_PACKAGE_NAME);
	}
}
