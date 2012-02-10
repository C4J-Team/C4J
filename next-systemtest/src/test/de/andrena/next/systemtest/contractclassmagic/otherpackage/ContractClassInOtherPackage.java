package de.andrena.next.systemtest.contractclassmagic.otherpackage;

import static de.andrena.next.Condition.pre;
import de.andrena.next.systemtest.contractclassmagic.ContractClassInOtherPackageSystemTest;

public class ContractClassInOtherPackage extends ContractClassInOtherPackageSystemTest.TargetClass {
	@Override
	protected void method(int arg) {
		if (pre()) {
			assert arg > 0;
		}
	}
}
