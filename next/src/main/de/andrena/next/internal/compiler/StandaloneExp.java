package de.andrena.next.internal.compiler;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;

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
