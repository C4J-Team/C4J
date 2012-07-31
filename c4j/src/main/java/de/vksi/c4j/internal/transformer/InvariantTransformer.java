package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.BehaviorFilter.DYNAMIC;
import static de.vksi.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import static de.vksi.c4j.internal.util.BehaviorFilter.VISIBLE;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Pure;
import de.vksi.c4j.internal.ContractErrorHandler.ContractErrorSource;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.TryExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.UnchangedCache;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ListOrderedSet;

/**
 * Transforming a method to look like the following block-comment:
 */
/*
try {
	if (canExecuteCondition()) {
		try {
			invariant1ForContract1$before();
			invariant2ForContract1$before();
			invariant1ForContract2$before();
			invariant2ForContract2$before();
		} catch (Throwable e) {
			handleContractException(e);
		} finally {
			afterContract();
		}
	}
	code();
} finally {
// <hasInvariant>
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
// </hasInvariant>
	afterContractMethod();
}
*/
public class InvariantTransformer extends ConditionTransformer {
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		StandaloneExp beforeInvariantCalls = getBeforeInvariantCall(contracts, affectedClass);
		Map<CtBehavior, List<CtBehavior>> contractMap = getContractMap(contracts, affectedClass);

		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredBehaviors(affectedClass, MODIFIABLE, DYNAMIC,
				VISIBLE)) {
			transformBehavior(affectedClass, affectedBehavior, contracts, beforeInvariantCalls, contractMap);
		}
	}

	private void transformBehavior(CtClass affectedClass, CtBehavior affectedBehavior,
			ListOrderedSet<ContractInfo> contracts, StandaloneExp beforeInvariantCalls,
			Map<CtBehavior, List<CtBehavior>> contractMap) throws CannotCompileException, Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName());
		}
		if (affectedBehavior.hasAnnotation(Pure.class)
				&& rootTransformer.getXmlConfiguration().getConfiguration(affectedClass).isPureSkipInvariants()) {
			transformWithoutInvariants(affectedClass, affectedBehavior, contractMap);
			return;
		}
		if (beforeInvariantCalls != null) {
			beforeInvariantCalls.insertBefore(affectedBehavior);
		}
		StandaloneExp invariantCalls = getInvariantCall(contracts, affectedClass, affectedBehavior);
		if (invariantCalls != null) {
			invariantCalls.insertFinally(affectedBehavior);
		} else {
			transformWithoutInvariants(affectedClass, affectedBehavior, contractMap);
		}
	}

	private void transformWithoutInvariants(CtClass affectedClass, CtBehavior affectedBehavior,
			Map<CtBehavior, List<CtBehavior>> contractMap) throws CannotCompileException {
		if (contractMap.containsKey(affectedBehavior)) {
			getAfterContractMethodCall().insertFinally(affectedBehavior);
		}
	}

	private StandaloneExp getBeforeInvariantCall(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass)
			throws NotFoundException {
		StandaloneExp invariantCalls = getInvariantContractCalls(contracts, affectedClass, BeforeClassInvariant.class);
		if (invariantCalls.isEmpty()) {
			return null;
		}
		TryExp tryInvariants = new TryExp(invariantCalls);
		catchWithHandleContractException(affectedClass, tryInvariants, ContractErrorSource.CLASS_INVARIANT);
		tryInvariants.addFinally(getAfterContractCall());
		return getCanExecuteConditionCall(tryInvariants);
	}

	private StandaloneExp setConstructorCall(CtBehavior affectedBehavior) {
		return new StaticCallExp(UnchangedCache.setClassInvariantConstructorCall, BooleanExp
				.valueOf(affectedBehavior instanceof CtConstructor)).toStandalone();
	}

	private StandaloneExp getInvariantCall(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass,
			CtBehavior affectedBehavior)
			throws NotFoundException {
		StandaloneExp invariantCalls = getInvariantContractCalls(contracts, affectedClass, ClassInvariant.class);
		if (invariantCalls.isEmpty()) {
			return null;
		}
		TryExp tryInvariants = new TryExp(setConstructorCall(affectedBehavior).append(invariantCalls));
		catchWithHandleContractException(affectedClass, tryInvariants, ContractErrorSource.CLASS_INVARIANT);
		tryInvariants.addFinally(getAfterContractCall().append(getAfterContractMethodCall()));
		return getCanExecuteConditionCall(tryInvariants).append(getAfterContractMethodCall());
	}

	private StandaloneExp getInvariantContractCalls(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass,
			Class<? extends Annotation> annotationClass)
			throws NotFoundException {
		StandaloneExp invariantCalls = new EmptyExp();
		for (CtMethod invariantMethod : getMethodsWithAnnotation(contracts, annotationClass)) {
			invariantCalls = invariantCalls.append(getContractCallExp(affectedClass,
					invariantMethod, getInvariantConditionCall(affectedClass, invariantMethod.getDeclaringClass())));
		}
		return invariantCalls;
	}

	private List<CtMethod> getMethodsWithAnnotation(ListOrderedSet<ContractInfo> contracts,
			Class<? extends Annotation> annotationClass) {
		List<CtMethod> invariantMethods = new ArrayList<CtMethod>();
		for (ContractInfo contractInfo : contracts) {
			for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
				if (contractMethod.hasAnnotation(annotationClass)) {
					invariantMethods.add(contractMethod);
				}
			}
		}
		return invariantMethods;
	}

	private StaticCallExp getInvariantConditionCall(CtClass affectedClass, CtClass contractClass) {
		return new StaticCallExp(Evaluator.getInvariant, NestedExp.THIS,
				new ValueExp(affectedClass.getSimpleName()), new ValueExp(contractClass),
				new ValueExp(affectedClass));
	}
}
