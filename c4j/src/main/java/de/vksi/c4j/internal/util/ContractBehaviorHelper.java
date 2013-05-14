package de.vksi.c4j.internal.util;

import javassist.CtBehavior;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.internal.transformer.ContractBehaviorTransformer;

public class ContractBehaviorHelper {

	public static boolean isContractConstructor(CtBehavior contractBehavior) {
		return ClassAnalyzer.isInitializer(contractBehavior)
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	public static boolean isContractClassInitializer(CtBehavior contractBehavior) {
		return ClassAnalyzer.isClassInitializer(contractBehavior)
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CLASS_INITIALIZER_REPLACEMENT_NAME);
	}

	public static String getContractBehaviorName(CtBehavior contractBehavior) {
		String contractBehaviorName;
		if (isContractConstructor(contractBehavior)) {
			contractBehaviorName = ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME;
		} else {
			contractBehaviorName = contractBehavior.getName();
		}
		return contractBehaviorName;
	}
}
