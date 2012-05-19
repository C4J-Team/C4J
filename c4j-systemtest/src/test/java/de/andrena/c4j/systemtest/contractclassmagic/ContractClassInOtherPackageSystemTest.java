package de.andrena.c4j.systemtest.contractclassmagic;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.systemtest.contractclassmagic.otherpackage.ContractClassInOtherPackage;
import de.andrena.c4j.ContractReference;

public class ContractClassInOtherPackageSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractClassInOtherPackage() {
		new TargetClass().method(1);
	}

	@Test(expected = AssertionError.class)
	public void testContractClassInOtherPackageFailing() {
		new TargetClass().method(0);
	}

	@ContractReference(ContractClassInOtherPackage.class)
	public static class TargetClass {
		protected void method(int arg) {
		}
	}
}
