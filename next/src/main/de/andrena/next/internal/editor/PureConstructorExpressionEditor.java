package de.andrena.next.internal.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;

public class PureConstructorExpressionEditor extends ExprEditor {
	protected Logger logger = Logger.getLogger(getClass());
	protected CtBehavior affectedBehavior;
	protected RootTransformer rootTransformer;

	public PureConstructorExpressionEditor(CtBehavior affectedBehavior, RootTransformer rootTransformer) {
		this.affectedBehavior = affectedBehavior;
		this.rootTransformer = rootTransformer;
	}

	@Override
	public void edit(ConstructorCall constructorCall) throws CannotCompileException {
		try {
			checkConstructor(constructorCall.where(), constructorCall.getConstructor(), constructorCall.getLineNumber());
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	protected void checkConstructor(CtBehavior affectedBehavior, CtConstructor constructor, int lineNumber)
			throws CannotCompileException, SecurityException, NoSuchMethodException, NotFoundException {
		if (constructor.isEmpty()) {
			if (constructor.getDeclaringClass().isFrozen())
				new PureConstructorExpressionEditor(affectedBehavior, rootTransformer).doit(
						constructor.getDeclaringClass(), constructor.getMethodInfo2());
			return;
		}
		if (rootTransformer.getConfiguration().getWhitelistConstructors().contains(constructor)) {
			return;
		}
		if (!constructor.hasAnnotation(Pure.class)) {
			pureError("illegal constructor access on constructor " + constructor.getLongName()
					+ " in pure method/constructor " + affectedBehavior.getLongName() + " on line " + lineNumber);
		}
	}

	private boolean isEqual(CtConstructor constructor, Constructor<?> whitelistConstructor) throws NotFoundException {
		hasSameClass(constructor, whitelistConstructor);
		return isEqual(constructor.getParameterTypes(), whitelistConstructor.getParameterTypes());
	}

	protected boolean isEqual(CtClass[] parameterTypes, Class<?>[] whitelistParamTypes) {
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

	protected boolean hasSameClass(CtMember member, Member whitelistMember) {
		return whitelistMember.getDeclaringClass().getName().equals(member.getDeclaringClass().getName());
	}

	protected void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))))
				.insertBefore(affectedBehavior);
	}
}
