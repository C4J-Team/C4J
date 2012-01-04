package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AssertExpTest {
	@Test
	public void testAssertExp() {
		assertEquals("\nassert (true == true);",
				new AssertExp(new CompareExp(BooleanExp.TRUE).eq(BooleanExp.TRUE)).getCode());
	}
}
