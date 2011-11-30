package next.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import next.internal.util.ObjectConverter;

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

	public static ArrayExp forParamTypes(CtMethod method) throws NotFoundException {
		List<NestedExp> paramTypes = new ArrayList<NestedExp>();
		for (CtClass paramClass : method.getParameterTypes()) {
			paramTypes.add(new ValueExp(paramClass));
		}
		return new ArrayExp(Class.class, paramTypes);
	}

	public static ArrayExp forArgs(CtMethod method) throws NotFoundException {
		List<NestedExp> args = new ArrayList<NestedExp>();
		int i = 0;
		for (CtClass paramClass : method.getParameterTypes()) {
			args.add(new StaticCallExp(ObjectConverter.toObject, NestedExp.arg(i + 1)));
			i++;
		}
		return new ArrayExp(Object.class, args);
	}

	@Override
	protected String getCode() {
		return code;
	}

}
