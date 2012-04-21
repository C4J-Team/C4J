package de.andrena.c4j.internal.compiler;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.expr.Expr;

public abstract class StandaloneExp extends Exp {

	public static final StandaloneExp proceed = CodeStandaloneExp.fromNested("$_ = $proceed($$)");

	public StandaloneExp append(StandaloneExp other) {
		return CodeStandaloneExp.fromStandalone(getCode() + other.getCode(), isEmpty() && other.isEmpty());
	}

	public StandaloneExp append(NestedExp other) {
		return append(other.toStandalone());
	}

	@Override
	public abstract String getCode();

	public void insertBefore(CtBehavior behavior) throws CannotCompileException {
		if (isEmpty()) {
			return;
		}
		if (behavior instanceof CtConstructor && !((CtConstructor) behavior).isClassInitializer()) {
			((CtConstructor) behavior).insertBeforeBody(getInsertCode(getCode()));
		} else {
			behavior.insertBefore(getInsertCode(getCode()));
		}
	}

	public void insertAfter(CtBehavior behavior) throws CannotCompileException {
		if (isEmpty()) {
			return;
		}
		behavior.insertAfter(getInsertCode(getCode()));
	}

	public void insertCatch(CtClass exceptionType, CtBehavior behavior) throws CannotCompileException {
		if (isEmpty()) {
			return;
		}
		behavior.addCatch(getInsertCode(getCode()), exceptionType);
	}

	public void insertFinally(CtBehavior behavior) throws CannotCompileException {
		if (isEmpty()) {
			return;
		}
		behavior.insertAfter(getInsertCode(getCode()), true);
	}

	public void replace(Expr expression) throws CannotCompileException {
		if (isEmpty()) {
			return;
		}
		expression.replace(getInsertCode(getCode()));
	}

	private String getInsertCode(String code) {
		return "{ " + code + " }";
	}

	public boolean isEmpty() {
		return false;
	}

	public static class CodeStandaloneExp extends StandaloneExp {
		private String code;
		private boolean empty;

		private CodeStandaloneExp(String code, boolean empty) {
			this.code = code;
			this.empty = empty;
		}

		protected static StandaloneExp fromStandalone(String code, boolean empty) {
			return new CodeStandaloneExp(code, empty);
		}

		protected static StandaloneExp fromNested(String code) {
			return new CodeStandaloneExp("\n" + code + ";", false);
		}

		@Override
		public boolean isEmpty() {
			return empty;
		}

		@Override
		public String getCode() {
			return code;
		}
	}
}
