package de.vksi.c4j.systemtest.contractclassmagic;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceIsInterfaceSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private ClassAnalyzer reflectionHelper = new ClassAnalyzer();

	@Test
	public void testContractReferenceIsInterface() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Ignoring contract "
				+ reflectionHelper.getSimplerName(ContractInterface.class) + " defined on "
				+ reflectionHelper.getSimplerName(TargetClass.class)
				+ " as the contract class is an interface.");
	}

	@ContractReference(ContractInterface.class)
	public static class TargetClass {
	}

	public interface ContractInterface {
	}
}
