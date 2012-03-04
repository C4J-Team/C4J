package de.andrena.c4j.systemtest.contractclassmagic.otherpackage;

import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.systemtest.contractclassmagic.ContractClassInOtherPackageSystemTest;

public class ContractClassInOtherPackage extends ContractClassInOtherPackageSystemTest.TargetClass {
	@Override
	protected void method(int arg) {
		if (pre()) {
			assert arg > 0;
		}
	}
}
