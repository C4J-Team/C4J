package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StaticCallTest {
	@Test
	public void testGetCode() {
		assertEquals("java.lang.String#format", new StaticCall(String.class, "format").getCode());
	}
}
