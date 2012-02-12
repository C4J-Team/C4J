package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AssignmentExpTest {

	@Test
	public void testAssignmentExp() {
		assertEquals("field = \"stringValue\"",
				new AssignmentExp(NestedExp.field("field"), new ValueExp("stringValue")).getCode());
	}
}
