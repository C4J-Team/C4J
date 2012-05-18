package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Test;

public class ValueExpTest {

	@Test
	public void testValueExpForClass() {
		assertEquals(ValueExpTest.class.getName() + ".class", new ValueExp(getClass()).getCode());
	}

	@Test
	public void testValueExpForCtClass() throws Exception {
		CtClass clazz = ClassPool.getDefault().get(getClass().getName());
		assertEquals(ValueExpTest.class.getName() + ".class", new ValueExp(clazz).getCode());
	}

	@Test
	public void testValueExpForString() {
		assertEquals("\"sampleString\"", new ValueExp("sampleString").getCode());
	}

	@Test
	public void testValueExpForInt() {
		assertEquals("5", new ValueExp(5).getCode());
	}

	@Test
	public void testValueExpForEnum() {
		assertEquals(SampleEnum.class.getName() + ".ENUM_VALUE", new ValueExp(SampleEnum.ENUM_VALUE).getCode());
	}

	public static enum SampleEnum {
		ENUM_VALUE;
	}

	@Test
	public void testValueExpForSpecificPrimitive() {
		assertEquals("(short) 5", new ValueExp(Integer.valueOf(5), short.class).getCode());
	}

	@Test
	public void testValueExpForStringArray() {
		assertEquals("new java.lang.String[] { \"firstValue\", \"secondValue\" }", new ValueExp(new String[] {
				"firstValue", "secondValue" }, String[].class).getCode());
	}

	@Test
	public void testValueExpForEmptyStringArray() {
		assertEquals("new java.lang.String[0]", new ValueExp(new String[0], String[].class).getCode());
	}

}
