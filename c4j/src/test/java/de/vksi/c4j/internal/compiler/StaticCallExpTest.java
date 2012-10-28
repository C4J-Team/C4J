package de.vksi.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Test;

public class StaticCallExpTest {
	@Test
	public void testStaticCallExp() {
		assertEquals("java.lang.String#format(\"value: %d\", 5)", new StaticCallExp(new StaticCall(String.class,
				"format"), new ValueExp("value: %d"), new ValueExp(5)).getCode());
	}

	@Test
	public void testStaticFieldExp() throws Exception {
		CtClass stringClass = ClassPool.getDefault().get(String.class.getName());
		assertEquals("java.lang.String#CASE_INSENSITIVE_ORDER", new StaticCallExp(stringClass
				.getField("CASE_INSENSITIVE_ORDER")).getCode());
	}
}
