package de.vksi.c4j.systemtest.contractclassmagic;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ExplicitlyLoadContractClassBeforeTargetClassSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	private ClassAnalyzer reflectionHelper = new ClassAnalyzer();

	@Test
	public void testLoadContractClassBeforeTargetClass1() {
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Class "
				+ reflectionHelper.getSimplerName(ContractClass.class)
				+ " cannot be its own contract-class. Try explicitly marking "
				+ reflectionHelper.getSimplerName(ContractClass.class) + " with @Contract.");
		// load ContractClass
		assertNotNull(ContractClass.class);
	}

	@Test
	public void testLoadContractClassBeforeTargetClass2() {
		// load ContractClass
		assertNotNull(ContractClass.class);
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Ignoring contract class "
				+ reflectionHelper.getSimplerName(ContractClass.class) + " defined on "
				+ reflectionHelper.getSimplerName(TargetClass.class)
				+ " as it has been loaded before the target type was loaded.");
		new TargetClass();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
	}

	private static class ContractClass extends TargetClass {
	}
}
