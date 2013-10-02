package de.vksi.c4j.internal.transformer.affected;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getSimpleName;
import javassist.CtBehavior;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.runtime.Evaluator;
import de.vksi.c4j.internal.runtime.ContractErrorHandler.ContractErrorSource;
import de.vksi.c4j.internal.transformer.util.ConditionExpression;

class BeforePostConditionCallProvider implements BeforeConditionCallProvider {
	@Override
	public StaticCallExp conditionCall(CtBehavior affectedBehavior, CtBehavior contractBehavior,
			NestedExp targetReference) throws NotFoundException {
		return new StaticCallExp(Evaluator.getPostCondition, targetReference, new ValueExp(
				getSimpleName(affectedBehavior)), new ValueExp(contractBehavior.getDeclaringClass()), new ValueExp(
				affectedBehavior.getDeclaringClass()), ConditionExpression.getReturnTypeExp(contractBehavior),
				ConditionExpression.getReturnValueExp(affectedBehavior));
	}

	@Override
	public ContractErrorSource getContractErrorSource() {
		return ContractErrorSource.POST_CONDITION;
	}

	@Override
	public IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecutePostCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}
}