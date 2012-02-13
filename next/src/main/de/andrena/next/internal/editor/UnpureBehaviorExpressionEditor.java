package de.andrena.next.internal.editor;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import de.andrena.next.AllowPureAccess;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.evaluator.PureEvaluator;

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
		StandaloneExp replacementExp = checkUnpureExp.append(StandaloneExp.proceed);
		replacementExp.replace(fieldAccess);
	}
}
