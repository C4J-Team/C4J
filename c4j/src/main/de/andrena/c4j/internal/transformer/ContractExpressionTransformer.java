package de.andrena.c4j.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.IfExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.editor.ContractMethodExpressionEditor;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(rootTransformer,
				contractInfo);
		logger.info("transforming behavior " + contractBehavior.getLongName());
		contractBehavior.instrument(expressionEditor);
		additionalStoreExpressions(expressionEditor);
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
		logger.info("before: " + storeConditionalExp.getCode());
		storeConditionalExp.insertBefore(contractBehavior);
	}

	private void additionalStoreExpressions(ContractMethodExpressionEditor expressionEditor) throws Exception {
		for (CtClass nestedContractClass : expressionEditor.getAndClearNestedInnerClasses()) {
			for (CtBehavior nestedBehavior : nestedContractClass.getDeclaredBehaviors()) {
				nestedBehavior.instrument(expressionEditor);
			}
			additionalStoreExpressions(expressionEditor);
		}
	}

}
