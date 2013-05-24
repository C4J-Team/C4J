package de.vksi.c4j.internal.transformer.affected;

import static de.vksi.c4j.internal.classfile.BehaviorFilter.DYNAMIC;
import static de.vksi.c4j.internal.classfile.BehaviorFilter.MODIFIABLE;
import static de.vksi.c4j.internal.classfile.BehaviorFilter.VISIBLE;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredBehaviors;

import java.util.List;
import java.util.Map;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractMethod;
import de.vksi.c4j.internal.types.ListOrderedSet;

/**
 * Transforming a method to look like the following block-comment: <code>
	if (canExecuteCondition()) {
		try {
			try {
				preContract1();
				handlePreConditionSuccess();
			} catch (Throwable e) {
				handlePreConditionException(e);
			}
			try {
				preContract2();
				handlePreConditionSuccess();
			} catch (Throwable e) {
				handlePreConditionException(e);
			}
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
	private static final Logger LOGGER = Logger.getLogger(DynamicConditionTransformer.class);

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass, Map<CtBehavior, List<ContractMethod>> contractMap) throws Exception {
		for (CtBehavior affectedBehavior : getDeclaredBehaviors(affectedClass, MODIFIABLE, DYNAMIC, VISIBLE)) {
			transform(affectedClass, affectedBehavior, contractMap.get(affectedBehavior));
		}
	}

	public void transform(CtClass affectedClass, CtBehavior affectedBehavior, List<ContractMethod> contractList)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("transforming behavior " + affectedBehavior.getLongName());
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
