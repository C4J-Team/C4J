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
import de.andrena.c4j.internal.util.ObjectConverter;
import de.andrena.c4j.internal.util.ReflectionHelper;

/**
 * Transforming a method to look like the following block-comment:
 */
/*
try {
	if (inPre()) {
		try {
			pre();
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
		if (inPost()) {
			try {
				post();
			} catch (Throwable e) {
				handleContractException(e);
			} finally {
				afterContract();
			}
		}
	}
} finally {
// mit Invariante:
	if (inInvariant()) {
		try {
			invariant();
		} catch (Throwable e) {
			handleContractException(e);
		} finally {
			afterContract();
			afterContractMethod();
		}
	}
// ohne Invariante (if inContract() in der Methode selbst):
	afterContractMethod();
}
*/
public class ConditionAndInvariantTransformer extends AffectedClassTransformerForSingleContract {
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception {
		List<CtMethod> classInvariantMethods = new ArrayList<CtMethod>();
		for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
			if (contractMethod.hasAnnotation(ClassInvariant.class)) {
				classInvariantMethods.add(contractMethod);
			}
		}
		StandaloneExp callInvariantExpression = null;
		if (!classInvariantMethods.isEmpty()) {
			callInvariantExpression = getInvariantCall(contractInfo, affectedClass, classInvariantMethods);
			logger.info("classInvariant after: " + callInvariantExpression.getCode());
		}

		Map<CtBehavior, CtBehavior> contractMap = new HashMap<CtBehavior, CtBehavior>();
		for (CtBehavior contractBehavior : contractInfo.getContractClass().getDeclaredBehaviors()) {
			CtBehavior affectedBehavior = affectedBehaviorLocator.getAffectedBehavior(contractInfo, affectedClass,
					contractBehavior);
			contractMap.put(affectedBehavior, contractBehavior);
		}
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredModifiableBehaviors(affectedClass)) {
			StandaloneExp behaviorInvariant = callInvariantExpression;
			if (affectedBehavior.hasAnnotation(Pure.class)
					&& rootTransformer.getConfigurationManager().getConfiguration(affectedClass).getPureBehaviors()
							.contains(PureBehavior.SKIP_INVARIANTS)) {
				behaviorInvariant = null;
			}
			transform(contractInfo, affectedClass, affectedBehavior, contractMap.get(affectedBehavior),
					behaviorInvariant);
		}
	}

	public void transform(ContractInfo contractInfo, CtClass affectedClass, CtBehavior affectedBehavior,
			CtBehavior contractBehavior, StandaloneExp invariantCall) throws Exception {
		if (contractBehavior != null) {
			insertPreAndPostCondition(contractInfo, affectedClass, affectedBehavior, contractBehavior);
		}
		if (invariantCall == null) {
			getAfterContractMethodCall(contractInfo).insertFinally(affectedBehavior);
		} else {
			invariantCall.insertFinally(affectedBehavior);
		}
	}

	private void insertPreAndPostCondition(ContractInfo contractInfo, CtClass affectedClass,
			CtBehavior affectedBehavior, CtBehavior contractBehavior) throws NotFoundException, CannotCompileException {
		logger.info("transforming behavior " + affectedBehavior.getLongName() + " for contract behavior "
				+ contractBehavior.getLongName());

		IfExp callPreCondition = getPreConditionCall(contractInfo, affectedClass, contractBehavior);
		StandaloneExp callPostCondition = getPostConditionCall(contractInfo, affectedClass, contractBehavior,
				affectedBehavior);
		StandaloneExp catchExceptionCall = getCatchExceptionCall();

		logger.info("insertCatch: " + catchExceptionCall.getCode());
		catchExceptionCall.insertCatch(rootTransformer.getPool().get(Throwable.class.getName()), affectedBehavior);
		logger.info("insertFinally: " + callPostCondition);
		callPostCondition.insertFinally(affectedBehavior);
		logger.info("insertBefore: " + callPreCondition.getCode());
		callPreCondition.insertBefore(affectedBehavior);
	}

	private IfExp getInvariantCall(ContractInfo contractInfo, CtClass affectedClass,
			List<CtMethod> classInvariantMethods) throws NotFoundException {
		StandaloneExp invariantCalls = new EmptyExp();
		for (CtMethod classInvariantMethod : classInvariantMethods) {
			invariantCalls = invariantCalls.append(getContractCallExp(contractInfo.getContractClass(), affectedClass,
					classInvariantMethod));
		}
		TryExp tryInvariants = new TryExp(invariantCalls);
		catchWithHandleContractException(affectedClass, tryInvariants);
		tryInvariants.addFinally(getAfterContractCall().append(getAfterContractMethodCall(contractInfo)));
		IfExp invariantCondition = new IfExp(new StaticCallExp(Evaluator.beforeInvariant, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass())));
		invariantCondition.addIfBody(tryInvariants);
		return invariantCondition;
	}

	private StandaloneExp getCatchExceptionCall() {
		StandaloneExp setExceptionCall = new StaticCallExp(Evaluator.setException, NestedExp.EXCEPTION_VALUE)
				.toStandalone();
		return setExceptionCall.append(new ThrowExp(NestedExp.EXCEPTION_VALUE));
	}

	private IfExp getPostConditionCall(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior,
			CtBehavior affectedBehavior) throws NotFoundException {
		TryExp callContractPost = new TryExp(getContractCallExp(contractInfo.getContractClass(), affectedClass,
				contractBehavior));
		catchWithHandleContractException(affectedClass, callContractPost);
		callContractPost.addFinally(getAfterContractCall());

		IfExp callPostCondition = new IfExp(new StaticCallExp(Evaluator.beforePost, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), getReturnTypeExp(contractBehavior),
				getReturnValueExp(affectedBehavior)));
		callPostCondition.addIfBody(callContractPost);
		return callPostCondition;
	}

	private void catchWithHandleContractException(CtClass affectedClass, TryExp contractCallExp) {
		contractCallExp.addCatch(Throwable.class, new StaticCallExp(ContractViolationHandler.handleContractException,
				contractCallExp.getCatchClauseVar(1), new ValueExp(affectedClass)).toStandalone());
	}

	private IfExp getPreConditionCall(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException {
		TryExp callContractPre = new TryExp(getContractCallExp(contractInfo.getContractClass(), affectedClass,
				contractBehavior));
		catchWithHandleContractException(affectedClass, callContractPre);
		callContractPre.addFinally(getAfterContractCall());
		IfExp callPreCondition = new IfExp(new StaticCallExp(Evaluator.beforePre, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), getReturnTypeExp(contractBehavior)));
		callPreCondition.addIfBody(callContractPre);
		return callPreCondition;
	}

	private StandaloneExp getAfterContractMethodCall(ContractInfo contractInfo) {
		StandaloneExp afterContractMethod = new StaticCallExp(Evaluator.afterContractMethod, new ValueExp(
				contractInfo.getContractClass())).toStandalone();
		return afterContractMethod;
	}

	private NestedExp getReturnValueExp(CtBehavior affectedBehavior) throws NotFoundException {
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			return NestedExp.NULL;
		}
		return new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
	}

}
