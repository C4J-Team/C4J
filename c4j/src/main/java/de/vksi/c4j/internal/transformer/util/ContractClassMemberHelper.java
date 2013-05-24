package de.vksi.c4j.internal.transformer.util;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.isDynamic;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.isStatic;
import javassist.CtBehavior;
import de.vksi.c4j.ConstructorContract;

public class ContractClassMemberHelper {
	public static final String BEFORE_INVARIANT_METHOD_SUFFIX = "$before".toString();
	public static final String TARGET_FIELD_NAME = "target$".toString();

	public static boolean isContractConstructor(CtBehavior contractBehavior) {
		return contractBehavior.hasAnnotation(ConstructorContract.class) && isDynamic(contractBehavior);
	}

	public static boolean isContractClassInitializer(CtBehavior contractBehavior) {
		return contractBehavior.hasAnnotation(ConstructorContract.class) && isStatic(contractBehavior);
	}

}
