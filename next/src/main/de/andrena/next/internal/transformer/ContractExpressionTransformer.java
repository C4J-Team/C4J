package de.andrena.next.internal.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.andrena.next.internal.ContractMethodExpressionEditor;
import de.andrena.next.internal.Evaluator;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.StaticCallExp;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	private ClassPool pool;

	public ContractExpressionTransformer(ClassPool pool) {
		this.pool = pool;
	}

	@Override
	public void transform(CtBehavior contractBehavior, CtClass targetClass) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(
				contractBehavior.getDeclaringClass(), pool);
		contractBehavior.instrument(expressionEditor);
		List<StaticCallExp> storeExpressions = expressionEditor.getStoreExpressions();
		storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getAdditionalContractClasses()));
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
		storeConditionalExp.insertBefore(contractBehavior);
	}

	private List<StaticCallExp> additionalStoreExpressions(Set<CtClass> classes) throws Exception {
		List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
		for (CtClass contractClass : classes) {
			ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractClass, pool);
			storeExpressions.addAll(expressionEditor.getStoreExpressions());
			storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getAdditionalContractClasses()));
		}
		return storeExpressions;
	}

}
