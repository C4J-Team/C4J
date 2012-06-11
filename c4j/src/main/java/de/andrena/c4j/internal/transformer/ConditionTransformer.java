package de.andrena.c4j.internal.transformer;

import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.c4j.internal.ContractErrorHandler;
import de.andrena.c4j.internal.ContractErrorHandler.ContractErrorSource;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.EmptyExp;
import de.andrena.c4j.internal.compiler.IfExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ThrowExp;
import de.andrena.c4j.internal.compiler.TryExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.ObjectConverter;
import de.andrena.c4j.internal.util.ReflectionHelper;

public abstract class ConditionTransformer extends AbstractAffectedClassTransformer {
	private ReflectionHelper reflectionHelper = new ReflectionHelper();
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	protected interface BeforeConditionCallProvider {
		StaticCallExp conditionCall(CtBehavior affectedBehavior, CtBehavior contractBehavior, NestedExp targetReference)
				throws NotFoundException;

		ContractErrorSource getContractErrorSource();

		IfExp getCanExecuteConditionCall(StandaloneExp body);
	}

	protected final BeforeConditionCallProvider beforePreConditionCallProvider = new BeforeConditionCallProvider() {
		@Override
		public StaticCallExp conditionCall(CtBehavior affectedBehavior,
					CtBehavior contractBehavior, NestedExp targetReference) throws NotFoundException {
			return new StaticCallExp(Evaluator.getPreCondition, targetReference, new ValueExp(
						reflectionHelper.getSimpleName(affectedBehavior)), new ValueExp(contractBehavior
						.getDeclaringClass()), new ValueExp(affectedBehavior.getDeclaringClass()),
						getReturnTypeExp(contractBehavior));
		}

		@Override
		public ContractErrorSource getContractErrorSource() {
			return ContractErrorSource.PRE_CONDITION;
		}

		@Override
		public IfExp getCanExecuteConditionCall(StandaloneExp body) {
			return ConditionTransformer.this.getCanExecuteConditionCall(body);
		}
	};
	protected final BeforeConditionCallProvider beforePostConditionCallProvider = new BeforeConditionCallProvider() {
		@Override
		public StaticCallExp conditionCall(CtBehavior affectedBehavior,
					CtBehavior contractBehavior, NestedExp targetReference) throws NotFoundException {
			return new StaticCallExp(Evaluator.getPostCondition, targetReference, new ValueExp(
						reflectionHelper.getSimpleName(affectedBehavior)), new ValueExp(contractBehavior
						.getDeclaringClass()), new ValueExp(affectedBehavior.getDeclaringClass()),
						getReturnTypeExp(contractBehavior), getReturnValueExp(affectedBehavior));
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
	};

	private NestedExp getReturnTypeExp(CtBehavior contractBehavior) throws NotFoundException {
		NestedExp returnTypeExp = NestedExp.NULL;
		if (contractBehavior instanceof CtMethod) {
			returnTypeExp = new ValueExp(((CtMethod) contractBehavior).getReturnType());
		}
		return returnTypeExp;
	}

	protected NestedExp getReturnValueExp(CtBehavior affectedBehavior) throws NotFoundException {
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			return NestedExp.NULL;
		}
		if (((CtMethod) affectedBehavior).getReturnType().isPrimitive()) {
			return new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		}
		return NestedExp.RETURN_VALUE;
	}

	protected void insertPreAndPostCondition(List<CtBehavior> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior) throws NotFoundException, CannotCompileException {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName()
					+ " for pre- and post-conditions with "
					+ contractList.size() + " contract-method calls");
		}

		StandaloneExp callPreCondition = getConditionCall(contractList, affectedClass, affectedBehavior,
				beforePreConditionCallProvider);
		StandaloneExp callPostCondition = getConditionCall(contractList, affectedClass, affectedBehavior,
				beforePostConditionCallProvider);
		StandaloneExp catchExceptionCall = getCatchExceptionCall();

		if (logger.isTraceEnabled()) {
			logger.trace("insertCatch: " + catchExceptionCall.getCode());
		}
		catchExceptionCall.insertCatch(rootTransformer.getPool().get(Throwable.class.getName()), affectedBehavior);
		if (logger.isTraceEnabled()) {
			logger.trace("insertFinally: " + callPostCondition);
		}
		callPostCondition.insertFinally(affectedBehavior);
		if (logger.isTraceEnabled()) {
			logger.trace("insertBefore: " + callPreCondition.getCode());
		}
		callPreCondition.insertBefore(affectedBehavior);
	}

	protected IfExp getConditionCall(List<CtBehavior> contractList, CtClass affectedClass,
			CtBehavior affectedBehavior, BeforeConditionCallProvider beforeConditionCallProvider)
			throws NotFoundException {
		StandaloneExp conditionCalls = new EmptyExp();
		for (CtBehavior contractBehavior : contractList) {
			conditionCalls = conditionCalls.append(getSingleConditionCall(affectedClass, affectedBehavior,
					beforeConditionCallProvider,
					contractBehavior));
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

	protected IfExp getCanExecuteConditionCall(StandaloneExp body) {
		IfExp canExecuteConditionCall = new IfExp(new StaticCallExp(Evaluator.canExecuteCondition));
		canExecuteConditionCall.addIfBody(body);
		return canExecuteConditionCall;
	}

	private StandaloneExp getCatchExceptionCall() {
		StandaloneExp setExceptionCall = new StaticCallExp(Evaluator.setException, NestedExp.EXCEPTION_VALUE)
				.toStandalone();
		return setExceptionCall.append(new ThrowExp(NestedExp.EXCEPTION_VALUE));
	}

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

	protected StandaloneExp getAfterContractMethodCall() {
		return new StaticCallExp(Evaluator.afterContractMethod).toStandalone();
	}

	protected StandaloneExp getAfterContractCall() {
		StandaloneExp afterContractExp = new StaticCallExp(Evaluator.afterContract).toStandalone();
		return afterContractExp;
	}

}