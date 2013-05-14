package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.classfile.BehaviorFilter.MODIFIABLE;
import static de.vksi.c4j.internal.classfile.BehaviorFilter.STATIC;
import static de.vksi.c4j.internal.classfile.BehaviorFilter.VISIBLE;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredBehaviors;

import java.util.List;
import java.util.Map;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ContractRegistry.ContractMethod;
import de.vksi.c4j.internal.util.ListOrderedSet;

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
public class StaticConditionTransformer extends PreAndPostConditionTransformer {
	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass, Map<CtBehavior, List<ContractMethod>> contractMap) throws Exception {
		for (CtBehavior affectedBehavior : getDeclaredBehaviors(affectedClass, MODIFIABLE, STATIC, VISIBLE)) {
			transform(affectedClass, affectedBehavior, contractMap.get(affectedBehavior));
		}
	}

	private void transform(CtClass affectedClass, CtBehavior affectedBehavior, List<ContractMethod> contractList)
			throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName());
		}
		if (contractList == null) {
			return;
		}
		insertPreAndPostCondition(contractList, affectedClass, affectedBehavior);
		getBeforeContractMethodCall().insertBefore(affectedBehavior);
		getAfterContractMethodCall().insertFinally(affectedBehavior);
	}

	@Override
	protected StandaloneExp getSingleConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			BeforeConditionCallProvider beforeConditionCallProvider, CtBehavior contractBehavior)
			throws NotFoundException {
		StaticCallExp getConditionCall = beforeConditionCallProvider.conditionCall(affectedBehavior, contractBehavior,
				NestedExp.NULL);
		return getContractCallExp(affectedClass, contractBehavior, getConditionCall);
	}

	@Override
	protected StandaloneExp getContractCallExp(CtClass affectedClass, CtBehavior contractBehavior,
			StaticCallExp conditionCall) throws NotFoundException {
		StaticCallExp conditionExecCall = new StaticCallExp((CtMethod) contractBehavior, getArgsList(affectedClass,
				contractBehavior));
		return conditionCall.toStandalone().append(conditionExecCall);
	}

}