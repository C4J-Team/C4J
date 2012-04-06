package de.andrena.c4j.internal.transformer;

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
import de.andrena.c4j.internal.ContractViolationHandler;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.CastExp;
import de.andrena.c4j.internal.compiler.EmptyExp;
import de.andrena.c4j.internal.compiler.IfExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ThrowExp;
import de.andrena.c4j.internal.compiler.TryExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.AffectedBehaviorLocator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.ObjectConverter;
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
// without Invariant (if canExecuteCondition() is necessary and checked within afterContractMethod()):
	afterContractMethod();
}
*/
public class ConditionAndInvariantTransformer extends AbstractAffectedClassTransformer {
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();
	private BeforeConditionCallProvider beforePreConditionCallProvider = new BeforeConditionCallProvider() {
		@Override
		public StaticCallExp conditionCall(CtBehavior affectedBehavior,
				CtBehavior contractBehavior) throws NotFoundException {
			return new StaticCallExp(Evaluator.getPreCondition, NestedExp.THIS, new ValueExp(
					reflectionHelper.getSimpleName(affectedBehavior)), new ValueExp(contractBehavior
					.getDeclaringClass()), new ValueExp(affectedBehavior.getDeclaringClass()),
					getReturnTypeExp(contractBehavior));
		}
	};
	private BeforeConditionCallProvider beforePostConditionCallProvider = new BeforeConditionCallProvider() {
		@Override
		public StaticCallExp conditionCall(CtBehavior affectedBehavior,
				CtBehavior contractBehavior) throws NotFoundException {
			return new StaticCallExp(Evaluator.getPostCondition, NestedExp.THIS, new ValueExp(
					reflectionHelper.getSimpleName(affectedBehavior)), new ValueExp(contractBehavior
					.getDeclaringClass()), new ValueExp(affectedBehavior.getDeclaringClass()),
					getReturnTypeExp(contractBehavior), getReturnValueExp(affectedBehavior));
		}
	};

	private interface BeforeConditionCallProvider {
		StaticCallExp conditionCall(CtBehavior affectedBehavior, CtBehavior contractBehavior)
				throws NotFoundException;
	}

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		StandaloneExp callInvariantExpression = getInvariantCall(contracts, affectedClass);

		Map<CtBehavior, List<CtBehavior>> contractMap = getContractMap(contracts, affectedClass);
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredModifiableBehaviors(affectedClass)) {
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

	private Map<CtBehavior, List<CtBehavior>> getContractMap(
			ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws NotFoundException, CannotCompileException {
		Map<CtBehavior, List<CtBehavior>> contractMap = new HashMap<CtBehavior, List<CtBehavior>>();
		for (ContractInfo contractInfo : contracts) {
			for (CtBehavior contractBehavior : contractInfo.getContractClass().getDeclaredBehaviors()) {
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
		logger.trace("transforming behavior " + affectedBehavior.getLongName());
		if (contractList != null) {
			insertPreAndPostCondition(contractList, affectedClass, affectedBehavior);
			if (invariantCall == null) {
				getAfterContractMethodCall().insertFinally(affectedBehavior);
			}
		}
		if (invariantCall != null) {
			invariantCall.insertFinally(affectedBehavior);
		}
	}

	private void insertPreAndPostCondition(List<CtBehavior> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior) throws NotFoundException, CannotCompileException {
		logger.trace("transforming behavior " + affectedBehavior.getLongName() + " for pre- and post-conditions with "
				+ contractList.size() + " contract-method calls");

		StandaloneExp callPreCondition = getConditionCall(contractList, affectedClass, affectedBehavior,
				beforePreConditionCallProvider);
		StandaloneExp callPostCondition = getConditionCall(contractList, affectedClass, affectedBehavior,
				beforePostConditionCallProvider);
		StandaloneExp catchExceptionCall = getCatchExceptionCall();

		logger.trace("insertCatch: " + catchExceptionCall.getCode());
		catchExceptionCall.insertCatch(rootTransformer.getPool().get(Throwable.class.getName()), affectedBehavior);
		logger.trace("insertFinally: " + callPostCondition);
		callPostCondition.insertFinally(affectedBehavior);
		logger.trace("insertBefore: " + callPreCondition.getCode());
		callPreCondition.insertBefore(affectedBehavior);
	}

	private IfExp getInvariantCall(ListOrderedSet<ContractInfo> contracts, CtClass affectedClass)
			throws NotFoundException {
		StandaloneExp invariantCalls = new EmptyExp();
		boolean invariantFound = false;
		for (ContractInfo contractInfo : contracts) {
			for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
				if (contractMethod.hasAnnotation(ClassInvariant.class)) {
					StaticCallExp invariantCall = new StaticCallExp(Evaluator.getInvariant, NestedExp.THIS,
							new ValueExp(affectedClass.getSimpleName()), new ValueExp(contractInfo.getContractClass()),
							new ValueExp(affectedClass));
					invariantCalls = invariantCalls.append(getContractCallExp(affectedClass,
							contractMethod, invariantCall));
					invariantFound = true;
				}
			}
		}
		if (!invariantFound) {
			return null;
		}
		TryExp tryInvariants = new TryExp(invariantCalls);
		catchWithHandleContractException(affectedClass, tryInvariants);
		tryInvariants.addFinally(getAfterContractCall().append(getAfterContractMethodCall()));
		return getCanExecuteConditionCall(tryInvariants);
	}

	private StandaloneExp getCatchExceptionCall() {
		StandaloneExp setExceptionCall = new StaticCallExp(Evaluator.setException, NestedExp.EXCEPTION_VALUE)
				.toStandalone();
		return setExceptionCall.append(new ThrowExp(NestedExp.EXCEPTION_VALUE));
	}

	private void catchWithHandleContractException(CtClass affectedClass, TryExp contractCallExp) {
		contractCallExp.addCatch(Throwable.class, new StaticCallExp(ContractViolationHandler.handleContractException,
				contractCallExp.getCatchClauseVar(1), new ValueExp(affectedClass)).toStandalone());
	}

	private IfExp getConditionCall(List<CtBehavior> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior, BeforeConditionCallProvider beforeConditionCallProvider)
			throws NotFoundException {
		StandaloneExp conditionCalls = new EmptyExp();
		for (CtBehavior contractBehavior : contractList) {
			StaticCallExp getConditionCall = beforeConditionCallProvider.conditionCall(affectedBehavior,
					contractBehavior);
			conditionCalls = conditionCalls
					.append(getContractCallExp(affectedClass, contractBehavior, getConditionCall));
		}
		TryExp tryPreCondition = new TryExp(conditionCalls);
		catchWithHandleContractException(affectedClass, tryPreCondition);
		tryPreCondition.addFinally(getAfterContractCall());
		return getCanExecuteConditionCall(tryPreCondition);
	}

	private StandaloneExp getAfterContractMethodCall() {
		return new StaticCallExp(Evaluator.afterContractMethod).toStandalone();
	}

	private NestedExp getReturnValueExp(CtBehavior affectedBehavior) throws NotFoundException {
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			return NestedExp.NULL;
		}
		if (((CtMethod) affectedBehavior).getReturnType().isPrimitive()) {
			return new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		}
		return NestedExp.RETURN_VALUE;
	}

	private IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecuteCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}

	private StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall)
			throws NotFoundException {
		CastExp getContractInstance = new CastExp(contractBehavior.getDeclaringClass(), conditionCall);
		return getContractInstance.appendCall(reflectionHelper.getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}

	private List<NestedExp> getArgsList(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (reflectionHelper.isContractConstructor(contractBehavior)
				&& reflectionHelper.constructorHasAdditionalParameter(affectedClass)) {
			return NestedExp.getArgsList(contractBehavior, 2);
		}
		return NestedExp.getArgsList(contractBehavior, 1);
	}

	private StandaloneExp getAfterContractCall() {
		StandaloneExp afterContractExp = new StaticCallExp(Evaluator.afterContract).toStandalone();
		return afterContractExp;
	}

	private NestedExp getReturnTypeExp(CtBehavior contractBehavior) throws NotFoundException {
		NestedExp returnTypeExp = NestedExp.NULL;
		if (contractBehavior instanceof CtMethod) {
			returnTypeExp = new ValueExp(((CtMethod) contractBehavior).getReturnType());
		}
		return returnTypeExp;
	}

}
