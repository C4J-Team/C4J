package de.vksi.c4j.internal.transformer.affected;

import static de.vksi.c4j.internal.runtime.ContractErrorHandler.ContractErrorSource.PRE_CONDITION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ThrowExp;
import de.vksi.c4j.internal.compiler.TryExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.contracts.ContractMethod;
import de.vksi.c4j.internal.runtime.ContractErrorHandler;
import de.vksi.c4j.internal.runtime.Evaluator;

public abstract class PreAndPostConditionTransformer extends ConditionTransformer {
	private static final Logger LOGGER = Logger.getLogger(PreAndPostConditionTransformer.class);

	private final BeforeConditionCallProvider beforePreConditionCallProvider = new BeforePreConditionCallProvider();
	private final BeforeConditionCallProvider beforePostConditionCallProvider = new BeforePostConditionCallProvider();
	private final BeforeConditionCallProvider beforeInitializerCallProvider = new BeforeInitializerCallProvider();

	protected void insertPreAndPostCondition(List<ContractMethod> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior) throws NotFoundException, CannotCompileException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("transforming behavior " + affectedBehavior.getLongName()
					+ " for pre- and post-conditions with " + contractList.size() + " contract-method calls");
		}

		getCatchExceptionCall().insertCatch(ClassFilePool.INSTANCE.getClass(Throwable.class), affectedBehavior);
		getConditionCall(getPostConditions(contractList), affectedClass, affectedBehavior,
				beforePostConditionCallProvider).insertFinally(affectedBehavior);
		getPreConditionCall(getPreConditions(contractList), affectedClass, affectedBehavior).insertBefore(
				affectedBehavior);

		insertInitializersForConstructor(contractList, affectedClass, affectedBehavior);
	}

	private StandaloneExp getPreConditionCall(List<CtMethod> preConditions, CtClass affectedClass,
			CtBehavior affectedBehavior) throws NotFoundException {
		StandaloneExp conditionCalls = new EmptyExp();
		for (Iterator<CtMethod> iterator = preConditions.iterator(); iterator.hasNext();) {
			CtMethod preCondition = iterator.next();
			conditionCalls = conditionCalls.append(getPreConditionCall(affectedClass, affectedBehavior, preCondition,
					!iterator.hasNext()));
		}
		if (conditionCalls.isEmpty()) {
			return conditionCalls;
		}
		TryExp tryConditionCalls = new TryExp(conditionCalls);
		tryConditionCalls.addFinally(getAfterContractCall());
		return beforePreConditionCallProvider.getCanExecuteConditionCall(tryConditionCalls);
	}

	private StandaloneExp getPreConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			CtBehavior contractBehavior, boolean lastCall) throws NotFoundException {
		StaticCallExp successCall = new StaticCallExp(ContractErrorHandler.handlePreConditionSuccess, new ValueExp(
				PRE_CONDITION), new ValueExp(affectedClass));
		TryExp tryPreCondition = new TryExp(getSingleConditionCall(affectedClass, affectedBehavior,
				beforePreConditionCallProvider, contractBehavior).append(successCall));
		tryPreCondition.addCatch(Throwable.class, new StaticCallExp(ContractErrorHandler.handlePreConditionException,
				new ValueExp(PRE_CONDITION), tryPreCondition.getCatchClauseVar(1), new ValueExp(affectedClass),
				new ValueExp(lastCall)).toStandalone());
		return tryPreCondition;
	}

	protected void insertInitializersForConstructor(List<ContractMethod> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior) throws NotFoundException, CannotCompileException {
		if (affectedBehavior instanceof CtConstructor) {
			List<CtMethod> initializers = getInitializers(contractList);
			if (!initializers.isEmpty()) {
				StandaloneExp callInitializer = getConditionCall(initializers, affectedClass, affectedBehavior,
						beforeInitializerCallProvider);
				callInitializer.insertBefore(affectedBehavior);
			}
		}
	}

	private List<CtMethod> getPreConditions(List<ContractMethod> contractList) {
		List<CtMethod> preConditions = new ArrayList<CtMethod>();
		for (ContractMethod contractMethod : contractList) {
			if (contractMethod.hasPreConditionOrDependencies()) {
				preConditions.add(contractMethod.getMethod());
			}
		}
		return preConditions;
	}

	private List<CtMethod> getInitializers(List<ContractMethod> contractList) {
		List<CtMethod> initializers = new ArrayList<CtMethod>();
		for (ContractMethod contractMethod : contractList) {
			if (!contractMethod.hasPreConditionOrDependencies() && !contractMethod.hasPostCondition()) {
				initializers.add(contractMethod.getMethod());
			}
		}
		return initializers;
	}

	private List<CtMethod> getPostConditions(List<ContractMethod> contractList) {
		List<CtMethod> postConditions = new ArrayList<CtMethod>();
		for (ContractMethod contractMethod : contractList) {
			if (contractMethod.hasPostCondition()) {
				postConditions.add(contractMethod.getMethod());
			}
		}
		return postConditions;
	}

	protected StandaloneExp getConditionCall(List<CtMethod> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior, BeforeConditionCallProvider beforeConditionCallProvider)
			throws NotFoundException {
		StandaloneExp conditionCalls = new EmptyExp();
		for (CtBehavior contractBehavior : contractList) {
			conditionCalls = conditionCalls.append(getSingleConditionCall(affectedClass, affectedBehavior,
					beforeConditionCallProvider, contractBehavior));
		}
		if (conditionCalls.isEmpty()) {
			return conditionCalls;
		}
		TryExp tryPreCondition = new TryExp(conditionCalls);
		catchWithHandleContractException(affectedClass, tryPreCondition, beforeConditionCallProvider
				.getContractErrorSource());
		tryPreCondition.addFinally(getAfterContractCall());
		return beforeConditionCallProvider.getCanExecuteConditionCall(tryPreCondition);
	}

	protected abstract StandaloneExp getSingleConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			BeforeConditionCallProvider beforeConditionCallProvider, CtBehavior contractBehavior)
			throws NotFoundException;

	private StandaloneExp getCatchExceptionCall() {
		StandaloneExp setExceptionCall = new StaticCallExp(Evaluator.setException, NestedExp.EXCEPTION_VALUE)
				.toStandalone();
		return setExceptionCall.append(new ThrowExp(NestedExp.EXCEPTION_VALUE));
	}

}