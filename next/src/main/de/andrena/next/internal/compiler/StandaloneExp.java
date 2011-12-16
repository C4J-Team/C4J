package de.andrena.next.internal.compiler;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;

public abstract class StandaloneExp extends Exp {

	public static StandaloneExp proceed = new CodeStandaloneExp("$_ = $proceed($$)");

	public StandaloneExp append(StandaloneExp other) {
		return new CodeStandaloneExp(getCode() + other.getCode());
	}

	public StandaloneExp append(NestedExp other) {
		return append(other.toStandalone());
	}

	@Override
	public abstract String getCode();

	public void insertBefore(CtBehavior behavior) throws CannotCompileException {
		if (behavior instanceof CtConstructor && !((CtConstructor) behavior).isClassInitializer()) {
			((CtConstructor) behavior).insertBeforeBody("{ " + getCode() + " }");
		} else {
			behavior.insertBefore("{ " + getCode() + " }");
		}
	}

	public void insertAfter(CtBehavior behavior) throws CannotCompileException {
		behavior.insertAfter("{" + getCode() + "}");
	}

	public static class CodeStandaloneExp extends StandaloneExp {
		private String code;

		protected CodeStandaloneExp(String code) {
			this.code = code + ";";
		}

		@Override
		public String getCode() {
			return code;
		}
	}
}
