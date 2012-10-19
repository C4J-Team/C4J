package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.BehaviorFilter.DYNAMIC;
import static de.vksi.c4j.internal.util.BehaviorFilter.MODIFIABLE;
import static de.vksi.c4j.internal.util.BehaviorFilter.VISIBLE;

import java.util.List;
import java.util.Map;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.ContractRegistry.ContractMethod;
import de.vksi.c4j.internal.util.ListOrderedSet;

/**
 * Transforming a method to look like the following block-comment: <code>
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
	}</code>
 */
public class DynamicConditionTransformer extends PreAndPostConditionTransformer {
	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass, Map<CtBehavior, List<ContractMethod>> contractMap) throws Exception {
		for (CtBehavior affectedBehavior : reflectionHelper.getDeclaredBehaviors(affectedClass, MODIFIABLE, DYNAMIC,
				VISIBLE)) {
			transform(affectedClass, affectedBehavior, contractMap.get(affectedBehavior));
		}
	}

	public void transform(CtClass affectedClass, CtBehavior affectedBehavior, List<ContractMethod> contractList)
			throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("transforming behavior " + affectedBehavior.getLongName());
		}
		if (contractList != null) {
			insertPreAndPostCondition(contractList, affectedClass, affectedBehavior);
		}
	}

	@Override
	protected StandaloneExp getSingleConditionCall(CtClass affectedClass, CtBehavior affectedBehavior,
			BeforeConditionCallProvider beforeConditionCallProvider, CtBehavior contractBehavior)
			throws NotFoundException {
		StaticCallExp getConditionCall = beforeConditionCallProvider.conditionCall(affectedBehavior, contractBehavior,
				NestedExp.THIS);
		return getContractCallExp(affectedClass, contractBehavior, getConditionCall);
	}

}
