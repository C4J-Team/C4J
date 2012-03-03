package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.AssignmentExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.ValueExp;

public class AssignmentExpTest {

	@Test
	public void testAssignmentExp() {
		assertEquals("field = \"stringValue\"",
				new AssignmentExp(NestedExp.field("field"), new ValueExp("stringValue")).getCode());
	}
}
