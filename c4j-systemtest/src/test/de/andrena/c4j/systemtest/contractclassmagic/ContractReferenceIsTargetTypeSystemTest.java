package de.andrena.c4j.systemtest.contractclassmagic;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.internal.util.ReflectionHelper;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceIsTargetTypeSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Test
	public void testContractReferenceIsTargetType() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Ignoring contract "
				+ reflectionHelper.getSimplerName(TargetClass.class) + " defined on "
				+ reflectionHelper.getSimplerName(TargetClass.class)
				+ " as the contract class is the same as the target class.");
	}

	@ContractReference(TargetClass.class)
	public static class TargetClass {
	}
}
