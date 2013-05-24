package de.vksi.c4j.systemtest.contractclassmagic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ExternalAccessOnPrivateNestedInstanceVariableSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testClassInvariantInPrivateMethod() {
		new TargetClass();
		assertThat(ContractClass.privateNestedInstanceVariable, is(true));
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
	}

	private static class ContractClass {
		private static boolean privateNestedInstanceVariable = true;
	}

}
