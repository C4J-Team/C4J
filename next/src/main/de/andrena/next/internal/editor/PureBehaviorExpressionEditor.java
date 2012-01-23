package de.andrena.next.internal.editor;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import de.andrena.next.Configuration;
import de.andrena.next.Pure;

public class PureBehaviorExpressionEditor extends PureConstructorExpressionEditor {

	public PureBehaviorExpressionEditor(CtBehavior affectedBehavior, Configuration configuration) {
		super(affectedBehavior, configuration);
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
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	@Override
	public void edit(NewExpr newExpr) throws CannotCompileException {
		try {
			editNewExpr(newExpr);
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	private void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException,
			SecurityException, NoSuchMethodException {
		CtMethod method = methodCall.getMethod();
		for (Member whitelistMember : configuration.getPureWhitelist()) {
			if (whitelistMember instanceof Method && isEqual(method, (Method) whitelistMember)) {
				return;
			}
		}
		if (!method.hasAnnotation(Pure.class)) {
			pureError("illegal method access on method " + method.getLongName() + " in pure method/constructor "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber());
		}
	}

	private void editNewExpr(NewExpr newExpr) throws NotFoundException, SecurityException, NoSuchMethodException,
			CannotCompileException {
		checkConstructor(affectedBehavior, newExpr.getConstructor(), newExpr.getLineNumber());
	}

	private boolean isEqual(CtMethod method, Method whitelistMethod) throws NotFoundException {
		if (!hasSameClass(method, whitelistMethod)) {
			return false;
		}
		if (!whitelistMethod.getName().equals(method.getName())) {
			return false;
		}
		return isEqual(method.getParameterTypes(), whitelistMethod.getParameterTypes());
	}
}
