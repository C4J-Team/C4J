package de.vksi.c4j.internal.transformer.affected;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.constructorHasAdditionalParameter;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.getContractBehaviorName;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.CastExp;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.TryExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.runtime.ContractErrorHandler;
import de.vksi.c4j.internal.runtime.ContractErrorHandler.ContractErrorSource;
import de.vksi.c4j.internal.runtime.Evaluator;
import de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper;

public abstract class ConditionTransformer extends AbstractAffectedClassTransformer {
	protected void catchWithHandleContractException(CtClass affectedClass, TryExp contractCallExp,
			ContractErrorSource source) {
		contractCallExp
				.addCatch(Throwable.class, new StaticCallExp(ContractErrorHandler.handleContractException,
						new ValueExp(source), contractCallExp.getCatchClauseVar(1), new ValueExp(affectedClass))
						.toStandalone());
	}

	protected List<NestedExp> getArgsList(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (ContractClassMemberHelper.isContractConstructor(contractBehavior)
				&& constructorHasAdditionalParameter(affectedClass)) {
			return NestedExp.getArgsList(contractBehavior, 2);
		}
		return NestedExp.getArgsList(contractBehavior, 1);
	}

	protected StandaloneExp getBeforeContractMethodCall() {
		return new StaticCallExp(Evaluator.beforeContractMethod).toStandalone();
	}

	protected StandaloneExp getAfterContractMethodCall() {
		return new StaticCallExp(Evaluator.afterContractMethod).toStandalone();
	}

	protected StandaloneExp getAfterContractCall() {
		return new StaticCallExp(Evaluator.afterContract).toStandalone();
	}

	protected IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecuteCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}

	protected StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall) throws NotFoundException {
		CastExp getContractInstance = new CastExp(contractBehavior.getDeclaringClass(), conditionCall);
		return getContractInstance.appendCall(getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}
}