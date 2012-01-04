package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThrowExpTest {
	@Test
	public void testThrowExp() {
		assertEquals("\nthrow new java.lang.AssertionError();",
				new ThrowExp(new ConstructorExp(AssertionError.class)).getCode());
	}
}
