package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javassist.ClassPool;

import org.junit.Test;

import de.andrena.next.internal.compiler.NestedExp.CodeNestedExp;

public class NestedExpTest {

	@Test
	public void testThis() {
		assertEquals("this", NestedExp.THIS.getCode());
	}

	@Test
	public void testNull() {
		assertEquals("null", NestedExp.NULL.getCode());
	}

	@Test
	public void testReturnValue() {
		assertEquals("$_", NestedExp.RETURN_VALUE.getCode());
	}

	@Test
	public void testArg() {
		assertEquals("$3", NestedExp.arg(3).getCode());
	}

	@Test
	public void testField() {
		assertEquals("this.someField", NestedExp.field("someField").getCode());
	}

	@Test
	public void testFieldForParentClass() throws Exception {
		assertEquals("de.andrena.next.internal.compiler.NestedExpTest.this.someField",
				NestedExp.field("someField", ClassPool.getDefault().get(getClass().getName())).getCode());
	}

	@Test
	public void testFieldForParentNestedClass() throws Exception {
		assertEquals("de.andrena.next.internal.compiler.NestedExpTest.NestedClass.this.someField",
				NestedExp.field("someField", ClassPool.getDefault().get(NestedClass.class.getName())).getCode());
	}

	public static class NestedClass {
	}

	@Test
	public void testMethod() {
		assertEquals("someMethod(\"firstValue\", \"secondValue\")",
				NestedExp.method("someMethod", new ValueExp("firstValue"), new ValueExp("secondValue")).getCode());
	}

	@Test
	public void testStandalone() {
		assertEquals("\nthis;", BooleanExp.THIS.toStandalone().getCode());
	}

	@Test
	public void testGetCodeForParams() {
		NestedExp exp = new ValueExp("dummy");
		assertEquals("(\"firstValue\", \"secondValue\")",
				exp.getCodeForParams(new ValueExp("firstValue"), new ValueExp("secondValue")));
	}

	@Test
	public void testGetCodeForValues() {
		NestedExp exp = new ValueExp("dummy");
		assertEquals("\"firstValue\", \"secondValue\"",
				exp.getCodeForValues(new ValueExp("firstValue"), new ValueExp("secondValue")));
	}

	@Test
	public void testAppendCallWithArray() {
		assertEquals("\"stringValue\".endsWith(\"Value\")",
				new ValueExp("stringValue").appendCall("endsWith", new ValueExp("Value")).getCode());
	}

	@Test
	public void testAppendCallWithList() {
		assertEquals("\"stringValue\".endsWith(\"Value\")",
				new ValueExp("stringValue").appendCall("endsWith", Arrays.<NestedExp> asList(new ValueExp("Value")))
						.getCode());
	}

	@Test
	public void testCodeNestedExp() {
		assertEquals("sample code", new CodeNestedExp("sample code").getCode());
	}
}
