package de.andrena.c4j.internal.util;

import de.andrena.c4j.internal.compiler.StaticCall;

public class ObjectConverter {
	public static final StaticCall toObject = new StaticCall(ObjectConverter.class, "toObject");

	public static Boolean toObject(boolean value) {
		return Boolean.valueOf(value);
	}

	public static Byte toObject(byte value) {
		return Byte.valueOf(value);
	}

	public static Character toObject(char value) {
		return Character.valueOf(value);
	}

	public static Double toObject(double value) {
		return Double.valueOf(value);
	}

	public static Float toObject(float value) {
		return Float.valueOf(value);
	}

	public static Integer toObject(int value) {
		return Integer.valueOf(value);
	}

	public static Long toObject(long value) {
		return Long.valueOf(value);
	}

	public static Object toObject(Object value) {
		return value;
	}

	public static Short toObject(short value) {
		return Short.valueOf(value);
	}

	public static boolean toPrimitive(boolean value) {
		return value;
	}

	public static boolean toPrimitive(Boolean value) {
		return value.booleanValue();
	}

	public static byte toPrimitive(byte value) {
		return value;
	}

	public static byte toPrimitive(Byte value) {
		return value.byteValue();
	}

	public static char toPrimitive(char value) {
		return value;
	}

	public static char toPrimitive(Character value) {
		return value.charValue();
	}

	public static double toPrimitive(double value) {
		return value;
	}

	public static double toPrimitive(Double value) {
		return value.doubleValue();
	}

	public static float toPrimitive(float value) {
		return value;
	}

	public static float toPrimitive(Float value) {
		return value.floatValue();
	}

	public static int toPrimitive(int value) {
		return value;
	}

	public static int toPrimitive(Integer value) {
		return value.intValue();
	}

	public static long toPrimitive(long value) {
		return value;
	}

	public static long toPrimitive(Long value) {
		return value.longValue();
	}

	public static Object toPrimitive(Object value) {
		return value;
	}

	public static short toPrimitive(short value) {
		return value;
	}

	public static short toPrimitive(Short value) {
		return value.shortValue();
	}
}