package next.internal.compiler;

import java.util.List;

public class ArrayExp extends NestedExp {

	private String code;

	public ArrayExp(Class<?> arrayClass, List<NestedExp> values) {
		this(arrayClass, values.toArray(new NestedExp[0]));
	}

	public ArrayExp(Class<?> arrayClass, NestedExp... values) {
		if (values.length == 0) {
			this.code = "new " + arrayClass.getName() + "[0]";
		} else {
			this.code = "new " + arrayClass.getName() + "[] { " + getCodeForValues(values) + " }";
		}
	}

	@Override
	protected String getCode() {
		return code;
	}

}
