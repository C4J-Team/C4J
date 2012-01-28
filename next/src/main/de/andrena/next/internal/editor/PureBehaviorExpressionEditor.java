package de.andrena.next.internal.editor;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import de.andrena.next.Pure;
import de.andrena.next.internal.RuntimeConfiguration;

public class PureBehaviorExpressionEditor extends PureConstructorExpressionEditor {

	private boolean allowOwnStateChange;

	public PureBehaviorExpressionEditor(CtBehavior affectedBehavior, RuntimeConfiguration configuration,
			boolean allowOwnStateChange) {
		super(affectedBehavior, configuration);
		this.allowOwnStateChange = allowOwnStateChange;
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
		if (constructorModifyingOwnClass(fieldAccess.getField())) {
			return;
		}
		if (fieldAccess.isWriter() && !isAllowedOwnStateChange(fieldAccess.getField())) {
			pureError("illegal field write access on field " + fieldAccess.getField().getName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + fieldAccess.getLineNumber());
		}
	}

	private boolean isAllowedOwnStateChange(CtMember member) throws NotFoundException {
		return allowOwnStateChange && affectedBehavior.getDeclaringClass().equals(member.getDeclaringClass());
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
		if (isSynthetic(method)) {
			return;
		}
		if (constructorModifyingOwnClass(method)) {
			return;
		}
		if (configuration.getWhitelistMethods().contains(method)) {
			return;
		}
		if (!method.hasAnnotation(Pure.class)) {
			pureError("illegal method access on method " + method.getLongName() + " in pure method/constructor "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber());
		}
	}

	private boolean constructorModifyingOwnClass(CtMember member) {
		return affectedBehavior instanceof CtConstructor
				&& member.getDeclaringClass().equals(affectedBehavior.getDeclaringClass());
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

	private boolean isSynthetic(CtBehavior behavior) {
		return (AccessFlag.of(behavior.getModifiers()) & AccessFlag.SYNTHETIC) > 0;
	}
}
