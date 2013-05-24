package de.vksi.c4j.systemtest.contractclassmagic;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceDoesntInheritNonFinalTargetType {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private ClassAnalyzer reflectionHelper = new ClassAnalyzer();

	@Test
	public void testContractReferenceDoesntInheritNonFinalTargetClass() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.WARN, "Contract type "
				+ reflectionHelper.getSimplerName(ContractClassForTargetClass.class)
				+ " does not inherit from its non-final target type "
				+ reflectionHelper.getSimplerName(TargetClass.class) + ".");
	}

	@Test
	public void testContractReferenceDoesntInheritFinalTargetClass() {
		new FinalTargetClass();
		transformerAwareRule.banGlobalLog(Level.WARN, "Contract type "
				+ reflectionHelper.getSimplerName(ContractClassForFinalTargetClass.class)
				+ " does not inherit from its non-final target type "
				+ reflectionHelper.getSimplerName(FinalTargetClass.class) + ".");
	}

	@Test
	public void testContractReferenceDoesntInheritNonFinalTargetInterface() {
		new TargetInterface() {
		};
		transformerAwareRule.expectGlobalLog(Level.WARN, "Contract type "
				+ reflectionHelper.getSimplerName(ContractClassForTargetInterface.class)
				+ " does not inherit from its non-final target type "
				+ reflectionHelper.getSimplerName(TargetInterface.class) + ".");
	}

	@ContractReference(ContractClassForTargetClass.class)
	private static class TargetClass {
	}

	private static class ContractClassForTargetClass {
	}

	@ContractReference(ContractClassForFinalTargetClass.class)
	public final static class FinalTargetClass {
	}

	private static class ContractClassForFinalTargetClass {
	}

	@ContractReference(ContractClassForTargetInterface.class)
	public interface TargetInterface {
	}

	private static class ContractClassForTargetInterface {
	}
}
