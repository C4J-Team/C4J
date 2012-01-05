package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.editor.ContractMethodExpressionEditor;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractInfo,
				contractBehavior);
		logger.info("transforming class " + contractBehavior.getLongName());
		contractBehavior.instrument(expressionEditor);
		additionalStoreExpressions(expressionEditor);
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
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
