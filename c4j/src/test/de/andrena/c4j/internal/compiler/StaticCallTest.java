package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.StaticCall;

public class StaticCallTest {
	@Test
	public void testGetCode() {
		assertEquals("java.lang.String#format", new StaticCall(String.class, "format").getCode());
	}
}
