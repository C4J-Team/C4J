package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BooleanExpTest {
	@Test
	public void testTrue() {
		assertEquals("true", BooleanExp.TRUE.getCode());
	}

	@Test
	public void testFalse() {
		assertEquals("false", BooleanExp.FALSE.getCode());
	}

	@Test
	public void testBooleanExp() {
		assertEquals("\"stringValue\"", new BooleanExp(new ValueExp("stringValue")).getCode());
	}

	@Test
	public void testAnd() {
		assertEquals("(true && false)", BooleanExp.TRUE.and(BooleanExp.FALSE).getCode());
	}

	@Test
	public void testOr() {
		assertEquals("(true || false)", BooleanExp.TRUE.or(BooleanExp.FALSE).getCode());
	}

	@Test
	public void testNot() {
		assertEquals("!(true)", BooleanExp.TRUE.not().getCode());
	}

	@Test
	public void testValueOf() throws Exception {
		assertEquals("true", BooleanExp.valueOf(true).getCode());
		assertEquals("false", BooleanExp.valueOf(false).getCode());
	}
}
