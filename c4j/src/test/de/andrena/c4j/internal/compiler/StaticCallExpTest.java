package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ValueExp;

public class StaticCallExpTest {
	@Test
	public void testStaticCallExp() {
		assertEquals("java.lang.String#format(\"value: %d\", 5)", new StaticCallExp(new StaticCall(String.class,
				"format"), new ValueExp("value: %d"), new ValueExp(5)).getCode());
	}
}
