package de.andrena.c4j.internal.compiler;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.expr.Expr;

public abstract class StandaloneExp extends Exp {

	public static final StandaloneExp proceed = CodeStandaloneExp.fromNested("$_ = $proceed($$)");

	public StandaloneExp append(StandaloneExp other) {
		return CodeStandaloneExp.fromStandalone(getCode() + other.getCode());
	}

	public StandaloneExp append(NestedExp other) {
		return append(other.toStandalone());
	}

	@Override
	public abstract String getCode();

	public void insertBefore(CtBehavior behavior) throws CannotCompileException {
		if (behavior instanceof CtConstructor && !((CtConstructor) behavior).isClassInitializer()) {
			((CtConstructor) behavior).insertBeforeBody(getInsertCode(getCode()));
		} else {
			behavior.insertBefore(getInsertCode(getCode()));
		}
	}

	public void insertAfter(CtBehavior behavior) throws CannotCompileException {
		behavior.insertAfter(getInsertCode(getCode()));
	}

	public void insertCatch(CtClass exceptionType, CtBehavior behavior) throws CannotCompileException {
		behavior.addCatch(getInsertCode(getCode()), exceptionType);
	}

	public void insertFinally(CtBehavior behavior) throws CannotCompileException {
		behavior.insertAfter(getInsertCode(getCode()), true);
	}

	public void replace(Expr expression) throws CannotCompileException {
		expression.replace(getInsertCode(getCode()));
	}

	private String getInsertCode(String code) {
		return "{ " + code + " }";
	}

	public static class CodeStandaloneExp extends StandaloneExp {
		private String code;

		private CodeStandaloneExp(String code) {
			this.code = code;
		}

		protected static StandaloneExp fromStandalone(String code) {
			return new CodeStandaloneExp(code);
		}

		protected static StandaloneExp fromNested(String code) {
			return new CodeStandaloneExp("\n" + code + ";");
		}

		@Override
		public String getCode() {
			return code;
		}
	}
}