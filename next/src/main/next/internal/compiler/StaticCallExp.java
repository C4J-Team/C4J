package next.internal.compiler;

public class StaticCallExp extends NestedExp {
	private String code;

	public StaticCallExp(StaticCall call, NestedExp... params) {
		code = "\n" + call.getCallClass().getName() + "#" + call.getCallMethod();
		code += getCodeForParams(params);
	}

	@Override
	protected String getCode() {
		return code;
	}

}
