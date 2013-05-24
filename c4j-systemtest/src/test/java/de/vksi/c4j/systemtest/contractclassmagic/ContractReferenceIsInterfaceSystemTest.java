package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getSimplerName;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceIsInterfaceSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractReferenceIsInterface() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Ignoring contract "
				+ getSimplerName(ContractInterface.class) + " defined on " + getSimplerName(TargetClass.class)
				+ " as the contract class is an interface.");
	}

	@ContractReference(ContractInterface.class)
	private static class TargetClass {
	}

	public interface ContractInterface {
	}
}
