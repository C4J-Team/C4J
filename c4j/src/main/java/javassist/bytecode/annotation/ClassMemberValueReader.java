package javassist.bytecode.annotation;

import javassist.bytecode.Descriptor;

public class ClassMemberValueReader {
	public static String readClassMemberValue(ClassMemberValue classMemberValue) {
		String v = classMemberValue.cp.getUtf8Info(classMemberValue.valueIndex);
		return Descriptor.toClassName(v);
	}
}