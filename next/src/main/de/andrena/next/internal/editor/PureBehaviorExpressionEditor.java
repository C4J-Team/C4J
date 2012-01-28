package de.andrena.next.internal.editor;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import de.andrena.next.AllowPureAccess;
import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.PureInspectorProvider;

public class PureBehaviorExpressionEditor extends PureConstructorExpressionEditor {

	private boolean allowOwnStateChange;

	public PureBehaviorExpressionEditor(CtBehavior affectedBehavior, RootTransformer rootTransformer,
			PureInspectorProvider pureInspectorProvider, boolean allowOwnStateChange) {
		super(affectedBehavior, rootTransformer, pureInspectorProvider);
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
		CtField field = fieldAccess.getField();
		if (constructorModifyingOwnClass(field)) {
			return;
		}
		if (!fieldAccess.isWriter()) {
			return;
		}
		if (isAllowedOwnStateChange(field)) {
			return;
		}
		if (field.hasAnnotation(AllowPureAccess.class)) {
			return;
		}
		pureError("illegal field write access on field " + field.getName() + " in pure method "
				+ affectedBehavior.getLongName() + " on line " + fieldAccess.getLineNumber());
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
		if (rootTransformer.getConfiguration().getWhitelistMethods().contains(method)) {
			return;
		}
		if (method.hasAnnotation(Pure.class)) {
			return;
		}
		if (pureInspectorProvider.getPureInspector().inspect(
				rootTransformer.getInvolvedTypeInspector().inspect(method.getDeclaringClass()), method) != null) {
			return;
		}
		pureError("illegal method access on unpure method " + method.getLongName() + " in pure method/constructor "
				+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber());
	}

	private boolean constructorModifyingOwnClass(CtMember member) {
		return affectedBehavior instanceof CtConstructor
				&& member.getDeclaringClass().equals(affectedBehavior.getDeclaringClass());
	}

	private void editNewExpr(NewExpr newExpr) throws NotFoundException, SecurityException, NoSuchMethodException,
			CannotCompileException {
		checkConstructor(affectedBehavior, newExpr.getConstructor(), newExpr.getLineNumber());
	}

	private boolean isSynthetic(CtBehavior behavior) {
		return (AccessFlag.of(behavior.getModifiers()) & AccessFlag.SYNTHETIC) > 0;
	}
}
