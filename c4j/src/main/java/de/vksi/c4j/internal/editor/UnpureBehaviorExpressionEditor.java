package de.vksi.c4j.internal.editor;

import static de.vksi.c4j.internal.util.ReflectionHelper.isClassInitializer;
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
		if (!fieldAccess.isStatic() && isOwnFieldAccess(fieldAccess)) {
			return;
		}
		// class initializers may initialize their own fields
		if (fieldAccess.isStatic() && isClassInitializer(fieldAccess.where()) && isOwnFieldAccess(fieldAccess)) {
			return;
		}
		if (fieldAccess.getField().hasAnnotation(AllowPureAccess.class)) {
			return;
		}
		StaticCallExp checkUnpureExp;
		if (fieldAccess.isStatic()) {
			checkUnpureExp = new StaticCallExp(PureEvaluator.checkUnpureStatic);
		} else {
			checkUnpureExp = new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.CALLING_OBJECT);
		}
		checkUnpureExp.toStandalone().append(StandaloneExp.PROCEED_AND_ASSIGN).replace(fieldAccess);
	}

	private boolean isOwnFieldAccess(FieldAccess fieldAccess) throws NotFoundException {
		return fieldAccess.getField().getDeclaringClass().equals(fieldAccess.where().getDeclaringClass());
	}

}
