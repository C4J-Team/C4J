package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StaticCallExpTest {
	@Test
	public void testStaticCallExp() {
		assertEquals("java.lang.String#format(\"value: %d\", 5)", new StaticCallExp(new StaticCall(String.class,
				"format"), new ValueExp("value: %d"), new ValueExp(5)).getCode());
	}
}
