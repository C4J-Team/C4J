package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getSimplerName;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractReferenceDoesntInheritNonFinalTargetType {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractReferenceDoesntInheritNonFinalTargetClass() {
		new TargetClass();
		transformerAwareRule.expectGlobalLog(Level.WARN, "Contract type "
				+ getSimplerName(ContractClassForTargetClass.class)
				+ " does not inherit from its non-final target type " + getSimplerName(TargetClass.class) + ".");
	}

	@Test
	public void testContractReferenceDoesntInheritFinalTargetClass() {
		new FinalTargetClass();
		transformerAwareRule.banGlobalLog(Level.WARN, "Contract type "
				+ getSimplerName(ContractClassForFinalTargetClass.class)
				+ " does not inherit from its non-final target type " + getSimplerName(FinalTargetClass.class) + ".");
	}

	@Test
	public void testContractReferenceDoesntInheritNonFinalTargetInterface() {
		new TargetInterface() {
		};
		transformerAwareRule.expectGlobalLog(Level.WARN, "Contract type "
				+ getSimplerName(ContractClassForTargetInterface.class)
				+ " does not inherit from its non-final target type " + getSimplerName(TargetInterface.class) + ".");
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
