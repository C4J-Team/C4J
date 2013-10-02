package de.vksi.c4j.internal.transformer.util;

import de.vksi.c4j.internal.compiler.StaticCall;

public class PrimitiveToObjectConverter {
	public static final StaticCall convert = new StaticCall(PrimitiveToObjectConverter.class, "convert");

	public static Boolean convert(boolean value) {
		return Boolean.valueOf(value);
	}

	public static Byte convert(byte value) {
		return Byte.valueOf(value);
	}

	public static Character convert(char value) {
		return Character.valueOf(value);
	}

	public static Double convert(double value) {
		return Double.valueOf(value);
	}

	public static Float convert(float value) {
		return Float.valueOf(value);
	}

	public static Integer convert(int value) {
		return Integer.valueOf(value);
	}

	public static Long convert(long value) {
		return Long.valueOf(value);
	}

	public static Object convert(Object value) {
		return value;
	}

	public static Short convert(short value) {
		return Short.valueOf(value);
	}
}