package de.andrena.next.systemtest.contractclassmagic;

import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.contractclassmagic.otherpackage.ContractClassInOtherPackage;

public class ContractClassInOtherPackageSystemTest {
	@Test
	public void testContractClassInOtherPackage() {
		new TargetClass().method(1);
	}

	@Test(expected = AssertionError.class)
	public void testContractClassInOtherPackageFailing() {
		new TargetClass().method(0);
	}

	@Contract(ContractClassInOtherPackage.class)
	public static class TargetClass {
		protected void method(int arg) {
		}
	}
}
