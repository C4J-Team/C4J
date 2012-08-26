package de.vksi.c4j.internal.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import de.vksi.c4j.internal.ContractErrorHandler;
import de.vksi.c4j.internal.ContractErrorHandler.ContractErrorSource;
import de.vksi.c4j.internal.compiler.CastExp;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.TryExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.util.AffectedBehaviorLocator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ListOrderedSet;
import de.vksi.c4j.internal.util.ReflectionHelper;

public abstract class ConditionTransformer extends AbstractAffectedClassTransformer {
	protected ReflectionHelper reflectionHelper = new ReflectionHelper();
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();

	protected void catchWithHandleContractException(CtClass affectedClass, TryExp contractCallExp,
			ContractErrorSource source) {
		contractCallExp
				.addCatch(Throwable.class, new StaticCallExp(ContractErrorHandler.handleContractException,
						new ValueExp(source), contractCallExp.getCatchClauseVar(1), new ValueExp(affectedClass))
						.toStandalone());
	}

	protected List<NestedExp> getArgsList(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (reflectionHelper.isContractConstructor(contractBehavior)
				&& reflectionHelper.constructorHasAdditionalParameter(affectedClass)) {
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
		StandaloneExp afterContractExp = new StaticCallExp(Evaluator.afterContract).toStandalone();
		return afterContractExp;
	}

	protected IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecuteCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}

	protected StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall) throws NotFoundException {
		CastExp getContractInstance = new CastExp(contractBehavior.getDeclaringClass(), conditionCall);
		return getContractInstance.appendCall(reflectionHelper.getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}

	protected Map<CtBehavior, List<CtBehavior>> getContractMap(ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws NotFoundException, CannotCompileException {
		Map<CtBehavior, List<CtBehavior>> contractMap = new HashMap<CtBehavior, List<CtBehavior>>();
		for (ContractInfo contractInfo : contracts) {
			for (CtBehavior contractBehavior : contractInfo.getContractClass().getDeclaredMethods()) {
				CtBehavior affectedBehavior = affectedBehaviorLocator.getAffectedBehavior(contractInfo, affectedClass,
						contractBehavior);
				if (affectedBehavior != null) {
					if (!contractMap.containsKey(affectedBehavior)) {
						contractMap.put(affectedBehavior, new ArrayList<CtBehavior>());
					}
					contractMap.get(affectedBehavior).add(contractBehavior);
				}
			}
		}
		return contractMap;
	}
}