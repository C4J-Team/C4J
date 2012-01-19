package de.andrena.next.internal.editor;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;

public class PureMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	private CtBehavior affectedBehavior;

	public PureMethodExpressionEditor(CtBehavior affectedBehavior) {
		this.affectedBehavior = affectedBehavior;
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editFieldAccess(FieldAccess fieldAccess) throws CannotCompileException, NotFoundException {
		if (fieldAccess.isWriter()) {
			pureError("illegal field write access on field " + fieldAccess.getField().getName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + fieldAccess.getLineNumber());
		}
	}

	@Override
	public void edit(MethodCall methodCall) throws CannotCompileException {
		try {
			editMethodCall(methodCall);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		if (!methodCall.getMethod().hasAnnotation(Pure.class)) {
			pureError("illegal method access on method " + methodCall.getMethod().getLongName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber());
		}
	}

	private void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))))
				.insertBefore(affectedBehavior);
	}
}
