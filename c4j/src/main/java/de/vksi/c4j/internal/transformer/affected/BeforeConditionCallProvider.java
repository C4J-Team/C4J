package de.vksi.c4j.internal.transformer.affected;

import javassist.CtBehavior;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.runtime.ContractErrorHandler.ContractErrorSource;

interface BeforeConditionCallProvider {
	StaticCallExp conditionCall(CtBehavior affectedBehavior, CtBehavior contractBehavior, NestedExp targetReference)
			throws NotFoundException;

	ContractErrorSource getContractErrorSource();

	IfExp getCanExecuteConditionCall(StandaloneExp body);
}