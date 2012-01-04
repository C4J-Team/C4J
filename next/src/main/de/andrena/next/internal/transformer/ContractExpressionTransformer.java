package de.andrena.next.internal.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.editor.ContractMethodExpressionEditor;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ContractExpressionTransformer extends ContractDeclaredBehaviorTransformer {

	private ClassPool pool;

	public ContractExpressionTransformer(ClassPool pool) {
		this.pool = pool;
	}

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractInfo, pool,
				contractBehavior);
		logger.info("transforming class " + contractBehavior.getLongName());
		contractBehavior.instrument(expressionEditor);
		List<StaticCallExp> storeExpressions = expressionEditor.getStoreExpressions();
		storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getNestedInnerClasses(), contractInfo,
				contractBehavior));
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
		storeConditionalExp.insertBefore(contractBehavior);
	}

	private List<StaticCallExp> additionalStoreExpressions(Set<CtClass> nestedInnerClasses, ContractInfo contractInfo,
			CtBehavior contractBehavior) throws Exception {
		List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
		for (CtClass nestedContractClass : nestedInnerClasses) {
			ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(contractInfo, pool,
					contractBehavior);
			for (CtBehavior nestedBehavior : nestedContractClass.getDeclaredBehaviors()) {
				nestedBehavior.instrument(expressionEditor);
			}
			storeExpressions.addAll(expressionEditor.getStoreExpressions());
			storeExpressions.addAll(additionalStoreExpressions(expressionEditor.getNestedInnerClasses(), contractInfo,
					contractBehavior));
		}
		return storeExpressions;
	}

}
