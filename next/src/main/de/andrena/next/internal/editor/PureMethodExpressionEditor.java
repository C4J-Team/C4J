package de.andrena.next.internal.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;
import de.andrena.next.Pure;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;

public class PureMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	private CtBehavior affectedBehavior;
	private Configuration configuration;

	public PureMethodExpressionEditor(CtBehavior affectedBehavior, Configuration configuration) {
		this.affectedBehavior = affectedBehavior;
		this.configuration = configuration;
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
		CtConstructor constructor = newExpr.getConstructor();
		for (Member whitelistMember : configuration.getPureWhitelist()) {
			if (whitelistMember instanceof Constructor && isEqual(constructor, (Constructor<?>) whitelistMember)) {
				return;
			}
		}
		if (!constructor.hasAnnotation(Pure.class)) {
			pureError("illegal constructor access on constructor " + constructor.getLongName()
					+ " in pure method/constructor " + affectedBehavior.getLongName() + " on line "
					+ newExpr.getLineNumber());
		}
	}

	private boolean isEqual(CtConstructor constructor, Constructor<?> whitelistConstructor) throws NotFoundException {
		hasSameClass(constructor, whitelistConstructor);
		return isEqual(constructor.getParameterTypes(), whitelistConstructor.getParameterTypes());
	}

	private boolean hasSameClass(CtMember member, Member whitelistMember) {
		return whitelistMember.getDeclaringClass().getName().equals(member.getDeclaringClass().getName());
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

	private boolean isEqual(CtClass[] parameterTypes, Class<?>[] whitelistParamTypes) {
		if (whitelistParamTypes.length != parameterTypes.length) {
			return false;
		}
		for (int i = 0; i < whitelistParamTypes.length; i++) {
			if (!whitelistParamTypes[i].getName().equals(parameterTypes[i].getName())) {
				return false;
			}
		}
		return true;
	}

	private void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))))
				.insertBefore(affectedBehavior);
	}
}
