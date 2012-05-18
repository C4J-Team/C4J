package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.ConstructorExp;
import de.andrena.c4j.internal.compiler.ThrowExp;

public class ThrowExpTest {
	@Test
	public void testThrowExp() {
		assertEquals("\nthrow new java.lang.AssertionError();",
				new ThrowExp(new ConstructorExp(AssertionError.class)).getCode());
	}
}
