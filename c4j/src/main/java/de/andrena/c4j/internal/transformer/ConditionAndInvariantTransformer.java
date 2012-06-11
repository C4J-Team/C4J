package de.andrena.c4j.internal.transformer;

import static de.andrena.c4j.internal.util.BehaviorFilter.DYNAMIC;
import static de.andrena.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import static de.andrena.c4j.internal.util.BehaviorFilter.VISIBLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.Configuration.PureBehavior;
import de.andrena.c4j.Pure;
import de.andrena.c4j.internal.ContractErrorHandler.ContractErrorSource;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.CastExp;
import de.andrena.c4j.internal.compiler.EmptyExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.TryExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.AffectedBehaviorLocator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.ReflectionHelper;

/**
 * Transforming a method to look like the following block-comment:
 */
/*
try {
	if (canExecuteCondition()) {
		try {
			preForContract1();
			preForContract2();
		} catch (Throwable e) {
			handleContractException(e);
		} finally {
			afterContract();
		}
	}
	try {
		code();
	} catch (Throwable e) {
		setException(e);
		throw e;
	} finally {
		if (canExecuteCondition()) {
			try {
				postForContract1();
				postForContract2();
			} catch (Throwable e) {
				handleContractException(e);
			} finally {
				afterContract();
			}
		}
	}
} finally {
// with Invariant:
	if (canExecuteCondition()) {
		try {
			invariant1ForContract1();
			invariant2ForContract1();
			invariant1ForContract2();
			invariant2ForContract2();
		} catch (Throwable e) {
			handleContractException(e);
		} finally {
			afterContract();
			afterContractMethod();
		}
	}
	afterContractMethod();
}
*/
public class ConditionAndInvariantTransformer extends ConditionTransformer {
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		StandaloneExp callInvariantExpression = getInvariantCall(contracts, affectedClass);

		Map<CtBehavior, List<CtBehavior>> contractMap = getContractMap(contracts, affectedClass);
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredBehaviors(affectedClass, MODIFIABLE, DYNAMIC,
				VISIBLE)) {
			StandaloneExp behaviorInvariant = callInvariantExpression;
			if (affectedBehavior.hasAnnotation(Pure.class)
					&& rootTransformer.getConfigurationManager().getConfiguration(affectedClass).getPureBehaviors()
							.contains(PureBehavior.SKIP_INVARIANTS)) {
				behaviorInvariant = null;
			}
			transform(affectedClass, affectedBehavior, contractMap.get(affectedBehavior),
					behaviorInvariant);
		}
	}

	private Map<CtBehavior, List<CtBehavior>> getContractMap(ListOrderedSet<ContractInfo> contracts,
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

	public void transform(CtClass affectedClass, CtBehavior affectedBehavior,
			List<CtBehavior> contractList, StandaloneExp invariantCall) throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName());
		}
		if (contractList != null) {
			insertPreAndPostCondition(contractList, affectedClass, affectedBehavior);
			if (invariantCall == null) {
				getAfterContractMethodCall().insertFinally(affectedBehavior);
			}
		}
		if (invariantCall != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("insertFinally: " + invariantCall);
			}
			invariantCall.insertFinally(affectedBehavior);
		}
	}

	private StandaloneExp getInvariantCall(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass)
			throws NotFoundException {
		StandaloneExp invariantCalls = getInvariantContractCalls(contracts, affectedClass);
		if (invariantCalls.isEmpty()) {
			return null;
		}
		TryExp tryInvariants = new TryExp(invariantCalls);
		catchWithHandleContractException(affectedClass, tryInvariants, ContractErrorSource.CLASS_INVARIANT);
		tryInvariants.addFinally(getAfterContractCall().append(getAfterContractMethodCall()));
		return getCanExecuteConditionCall(tryInvariants).append(getAfterContractMethodCall());
	}

	private StandaloneExp getInvariantContractCalls(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass)
			throws NotFoundException {
		StandaloneExp invariantCalls = new EmptyExp();
		for (ContractInfo contractInfo : contracts) {
			for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
				if (contractMethod.hasAnnotation(ClassInvariant.class)) {
					invariantCalls = invariantCalls.append(getContractCallExp(affectedClass,
							contractMethod, getInvariantConditionCall(affectedClass, contractInfo)));
				}
			}
		}
		return invariantCalls;
	}

	private StaticCallExp getInvariantConditionCall(CtClass affectedClass, ContractInfo contractInfo) {
		return new StaticCallExp(Evaluator.getInvariant, NestedExp.THIS,
				new ValueExp(affectedClass.getSimpleName()), new ValueExp(contractInfo.getContractClass()),
				new ValueExp(affectedClass));
	}

	@Override
	protected StandaloneExp getSingleConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			BeforeConditionCallProvider beforeConditionCallProvider,
			CtBehavior contractBehavior) throws NotFoundException {
		StaticCallExp getConditionCall = beforeConditionCallProvider.conditionCall(affectedBehavior,
				contractBehavior, NestedExp.THIS);
		return getContractCallExp(affectedClass, contractBehavior, getConditionCall);
	}

	private StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall)
			throws NotFoundException {
		CastExp getContractInstance = new CastExp(contractBehavior.getDeclaringClass(), conditionCall);
		return getContractInstance.appendCall(reflectionHelper.getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}
}
