package de.vksi.c4j.systemtest.contractclassmagic.otherpackage;

import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.systemtest.contractclassmagic.ContractClassInOtherPackageSystemTest;

public class ContractClassInOtherPackage extends ContractClassInOtherPackageSystemTest.TargetClass {
	@Override
	protected void method(int arg) {
		if (preCondition()) {
			assert arg > 0;
		}
	}
}
