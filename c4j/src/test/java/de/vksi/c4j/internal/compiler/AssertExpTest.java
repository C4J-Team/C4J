package de.vksi.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.vksi.c4j.internal.compiler.AssertExp;
import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.CompareExp;

public class AssertExpTest {
	@Test
	public void testAssertExp() {
		assertEquals("\nassert (true == true);",
				new AssertExp(new CompareExp(BooleanExp.TRUE).eq(BooleanExp.TRUE)).getCode());
	}
}
