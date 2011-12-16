package de.andrena.next.internal.compiler;

public class TryExp extends StandaloneExp {
	private StandaloneExp tryExp;
	private StandaloneExp finallyExp;

	public TryExp(StandaloneExp body) {
		this.tryExp = body;
	}

	public void addFinally(StandaloneExp finallyExp) {
		this.finallyExp = finallyExp;
	}

	@Override
	public String getCode() {
		String code = "\n" + "try {" + tryExp.getCode() + "\n" + "}";
		if (finallyExp != null) {
			code += " finally {" + finallyExp.getCode() + "\n" + "}";
		}
		return code;
	}

}
