package de.andrena.next.internal.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.internal.ContractMethodExpressionEditor;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.Evaluator;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.StaticCallExp;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	private ClassPool pool;

	public ContractExpressionTransformer(ClassPool pool) {
		this.pool = pool;
	}

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractInfo, pool);
		logger.info("transforming class " + contractBehavior.getLongName());
		contractBehavior.instrument(expressionEditor);
		List<StaticCallExp> storeExpressions = expressionEditor.getStoreExpressions();
		storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getNestedInnerClasses(), contractInfo));
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
		storeConditionalExp.insertBefore(contractBehavior);
	}

	private List<StaticCallExp> additionalStoreExpressions(Set<CtClass> nestedInnerClasses, ContractInfo contractInfo)
			throws Exception {
		List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
		for (CtClass nestedContractClass : nestedInnerClasses) {
			ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractInfo, pool);
			for (CtBehavior nestedBehavior : nestedContractClass.getDeclaredBehaviors()) {
				nestedBehavior.instrument(expressionEditor);
			}
			storeExpressions.addAll(expressionEditor.getStoreExpressions());
			storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getNestedInnerClasses(), contractInfo));
		}
		return storeExpressions;
	}

}
