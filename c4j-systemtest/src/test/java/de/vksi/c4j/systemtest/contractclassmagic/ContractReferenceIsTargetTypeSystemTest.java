package de.vksi.c4j.systemtest.contractclassmagic;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceIsTargetTypeSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractReferenceIsTargetType() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Ignoring contract "
				+ ClassAnalyzer.getSimplerName(TargetClass.class) + " defined on "
				+ ClassAnalyzer.getSimplerName(TargetClass.class)
				+ " as the contract class is the same as the target class.");
	}

	@ContractReference(TargetClass.class)
	private static class TargetClass {
	}
}
