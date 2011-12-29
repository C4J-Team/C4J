package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompareExpTest {

	@Test
	public void testCompareExp() {
		assertEquals("\"stringValue\"", new CompareExp(new ValueExp("stringValue")).getCode());
	}

	@Test
	public void testEq() {
		assertEquals("(4 == 5)", new CompareExp(new ValueExp(4)).eq(new ValueExp(5)).getCode());
	}

	@Test
	public void testGt() {
		assertEquals("(4 > 5)", new CompareExp(new ValueExp(4)).gt(new ValueExp(5)).getCode());
	}

	@Test
	public void testGe() {
		assertEquals("(4 >= 5)", new CompareExp(new ValueExp(4)).ge(new ValueExp(5)).getCode());
	}

	@Test
	public void testLt() {
		assertEquals("(4 < 5)", new CompareExp(new ValueExp(4)).lt(new ValueExp(5)).getCode());
	}

	@Test
	public void testLe() {
		assertEquals("(4 <= 5)", new CompareExp(new ValueExp(4)).le(new ValueExp(5)).getCode());
	}
}
