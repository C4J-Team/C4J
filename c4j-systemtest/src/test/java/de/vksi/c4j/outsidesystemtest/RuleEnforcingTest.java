package de.vksi.c4j.outsidesystemtest;

import org.junit.Test;

public class RuleEnforcingTest extends RuleEnforcingTestBase {
	private static final String SYSTEMTEST_PACKAGE_NAME = "de.vksi.c4j.systemtest";

	@Test
	public void allTestsUseRule() throws Exception {
		enforceRuleInPackage(SYSTEMTEST_PACKAGE_NAME);
	}
}
