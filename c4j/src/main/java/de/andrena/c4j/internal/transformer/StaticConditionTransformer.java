package de.andrena.c4j.internal.transformer;

import static de.andrena.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import static de.andrena.c4j.internal.util.BehaviorFilter.STATIC;

import java.util.Collections;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.util.AffectedBehaviorLocator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.ReflectionHelper;

/*
try {
	if (canExecuteCondition()) {
		try {
			preForContract();
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
				postForContract();
			} catch (Throwable e) {
				handleContractException(e);
			} finally {
				afterContract();
			}
		}
	}
} finally {
// (if canExecuteCondition() is necessary and checked within afterContractMethod()):
	afterContractMethod();
}
*/
public class StaticConditionTransformer extends ConditionTransformer {
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (ContractInfo contract : contracts) {
			if (contract.getTargetClass().equals(affectedClass)) {
				transform(contract, affectedClass);
			}
		}
	}

	private void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception {
		for (CtBehavior contractBehavior : reflectionHelper.getDeclaredMethods(contractInfo.getContractClass(),
				MODIFIABLE, STATIC)) {
			CtBehavior affectedBehavior = affectedBehaviorLocator.getAffectedBehavior(contractInfo, affectedClass,
					contractBehavior);
			if (affectedBehavior != null) {
				transform(affectedClass, affectedBehavior, contractBehavior);
			}
		}
	}

	public void transform(CtClass affectedClass, CtBehavior affectedBehavior, CtBehavior contractBehavior)
			throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName());
		}
		if (contractBehavior instanceof CtConstructor) {
			// TODO: handle static constructor
			return;
		}
		insertPreAndPostCondition(Collections.singletonList(contractBehavior), affectedClass, affectedBehavior);
		getAfterContractMethodCall().insertFinally(affectedBehavior);
	}

	@Override
	protected StandaloneExp getSingleConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			BeforeConditionCallProvider beforeConditionCallProvider,
			CtBehavior contractBehavior) throws NotFoundException {
		StaticCallExp getConditionCall = beforeConditionCallProvider.conditionCall(affectedBehavior,
				contractBehavior, NestedExp.NULL);
		return getContractCallExp(affectedClass, contractBehavior, getConditionCall);
	}

	private StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall) throws NotFoundException {
		StaticCallExp conditionExecCall = new StaticCallExp((CtMethod) contractBehavior, getArgsList(affectedClass,
				contractBehavior));
		return conditionCall.toStandalone().append(conditionExecCall);
	}

}