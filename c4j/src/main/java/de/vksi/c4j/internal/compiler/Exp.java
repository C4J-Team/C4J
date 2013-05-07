package de.vksi.c4j.internal.compiler;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.expr.Expr;

public abstract class Exp {
	protected abstract String getCode();

	@Override
	public String toString() {
		return getCode();
	}

	public abstract void insertBefore(CtBehavior behavior) throws CannotCompileException;

	public abstract void insertAfter(CtBehavior behavior) throws CannotCompileException;

	public abstract void insertCatch(CtClass exceptionType, CtBehavior behavior) throws CannotCompileException;

	public abstract void insertFinally(CtBehavior behavior) throws CannotCompileException;

	public abstract void replace(Expr expression) throws CannotCompileException;

}
