package next.internal.compiler;

import javassist.CtClass;

public class CastExp extends NestedExp {
	private String code;

	public CastExp(Class<?> castClass, NestedExp exp) {
		this.code = "((" + castClass.getCanonicalName() + ") " + exp.getCode() + ")";
	}

	public CastExp(CtClass castClass, NestedExp exp) {
		this.code = "((" + castClass.getName() + ") " + exp.getCode() + ")";
	}

	@Override
	protected String getCode() {
		return code;
	}

}
