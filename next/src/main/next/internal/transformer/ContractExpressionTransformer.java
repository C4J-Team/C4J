package next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import next.internal.ContractMethodExpressionEditor;
import next.internal.Evaluator;
import next.internal.compiler.IfExp;
import next.internal.compiler.StaticCallExp;

public class ContractExpressionTransformer extends ContractDeclaredMethodTransformer {

	@Override
	public void transform(CtMethod contractMethod, CtClass targetClass) throws Exception {
		ContractMethodExpressionEditor expressionEditor = new ContractMethodExpressionEditor(
				contractMethod.getDeclaringClass());
		contractMethod.instrument(expressionEditor);
		IfExp storeConditionalExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		for (StaticCallExp storeExp : expressionEditor.getStoreExpressions()) {
			storeConditionalExp.addIfBody(storeExp.toStandalone());
		}
		storeConditionalExp.insertBefore(contractMethod);
	}

}
