package de.andrena.c4j.internal.compiler;

public class IfExp extends StandaloneExp {
	private NestedExp condition;
	private StandaloneExp ifBody;
	private StandaloneExp elseBody;

	public IfExp(NestedExp condition) {
		this.condition = condition;
	}

	public void addIfBody(StandaloneExp body) {
		if (ifBody == null) {
			ifBody = body;
		} else {
			ifBody = ifBody.append(body);
		}
	}

	public void addElseBody(StandaloneExp body) {
		if (elseBody == null) {
			elseBody = body;
		} else {
			elseBody = elseBody.append(body);
		}
	}

	@Override
	public String getCode() {
		String code = "\n" + "if (" + condition.getCode() + ") {";
		if (ifBody != null) {
			code += ifBody.getCode();
		}
		code += "\n}";
		if (elseBody != null) {
			code += " else {";
			code += elseBody.getCode();
			code += "\n}";
		}
		return code;
	}

}
