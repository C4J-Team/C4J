package de.vksi.c4j.internal.transformer.util;

import javassist.CtBehavior;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;

public class ContractClassMemberHelper {
	public static final String CONSTRUCTOR_REPLACEMENT_NAME = "constructor$".toString();
	public static final String CLASS_INITIALIZER_REPLACEMENT_NAME = "classInitializer$".toString();
	public static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before".toString();
	public static final String TARGET_FIELD_NAME = "target$".toString();

	public static boolean isContractConstructor(CtBehavior contractBehavior) {
		return ClassAnalyzer.isInitializer(contractBehavior)
				|| contractBehavior.getName().equals(CONSTRUCTOR_REPLACEMENT_NAME);
	}

	public static boolean isContractClassInitializer(CtBehavior contractBehavior) {
		return ClassAnalyzer.isClassInitializer(contractBehavior)
				|| contractBehavior.getName().equals(CLASS_INITIALIZER_REPLACEMENT_NAME);
	}

	public static String getContractBehaviorName(CtBehavior contractBehavior) {
		String contractBehaviorName;
		if (ContractClassMemberHelper.isContractConstructor(contractBehavior)) {
			contractBehaviorName = CONSTRUCTOR_REPLACEMENT_NAME;
		} else {
			contractBehaviorName = contractBehavior.getName();
		}
		return contractBehaviorName;
	}

}
