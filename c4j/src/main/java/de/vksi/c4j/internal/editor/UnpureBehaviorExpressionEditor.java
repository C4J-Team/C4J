package de.vksi.c4j.internal.editor;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.evaluator.PureEvaluator;

public class UnpureBehaviorExpressionEditor extends ExprEditor {
	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editFieldAccess(FieldAccess fieldAccess) throws NotFoundException, CannotCompileException {
		if (fieldAccess.isReader()) {
			return;
		}
		// own fields can be written, as this is not a pure method and access is already prevented by PureInspector.checkUnpureAccess
		if (!fieldAccess.isStatic()
				&& fieldAccess.getField().getDeclaringClass().equals(fieldAccess.where().getDeclaringClass())) {
			return;
		}
		if (fieldAccess.getField().hasAnnotation(AllowPureAccess.class)) {
			return;
		}
		StandaloneExp checkUnpureExp;
		if (fieldAccess.isStatic()) {
			checkUnpureExp = new StaticCallExp(PureEvaluator.checkUnpureStatic).toStandalone();
		} else {
			checkUnpureExp = new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.CALLING_OBJECT)
					.toStandalone();
		}
		StandaloneExp replacementExp = checkUnpureExp.append(StandaloneExp.PROCEED_AND_ASSIGN);
		replacementExp.replace(fieldAccess);
	}
}
