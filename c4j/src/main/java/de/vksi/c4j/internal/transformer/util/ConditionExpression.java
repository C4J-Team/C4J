package de.vksi.c4j.internal.transformer.util;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.runtime.Evaluator;

public class ConditionExpression {

	public static NestedExp getReturnTypeExp(CtBehavior contractBehavior) throws NotFoundException {
		if (contractBehavior instanceof CtMethod) {
			return new ValueExp(((CtMethod) contractBehavior).getReturnType());
		}
		return NestedExp.NULL;
	}

	public static NestedExp getReturnValueExp(CtBehavior affectedBehavior) throws NotFoundException {
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			return NestedExp.NULL;
		}
		if (((CtMethod) affectedBehavior).getReturnType().isPrimitive()) {
			return new StaticCallExp(PrimitiveToObjectConverter.convert, NestedExp.RETURN_VALUE);
		}
		return NestedExp.RETURN_VALUE;
	}

	public static IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecuteCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}

}
