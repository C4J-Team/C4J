package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.CompareExp;
import de.andrena.c4j.internal.compiler.ValueExp;

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
	public void testNe() {
		assertEquals("(4 != 5)", new CompareExp(new ValueExp(4)).ne(new ValueExp(5)).getCode());
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

	@Test
	public void testIsEqual() {
		assertEquals("\"firstString\".equals(\"secondString\")",
				new CompareExp(new ValueExp("firstString")).isEqual(new ValueExp("secondString")).getCode());
	}

	@Test
	public void testIsNotEqual() {
		assertEquals("!\"firstString\".equals(\"secondString\")", new CompareExp(new ValueExp("firstString"))
				.isNotEqual(new ValueExp("secondString")).getCode());
	}
}
