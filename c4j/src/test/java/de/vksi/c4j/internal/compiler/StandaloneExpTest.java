package de.vksi.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.junit.Test;

import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StandaloneExp.CodeStandaloneExp;

public class StandaloneExpTest {

	@Test
	public void testProceed() {
		assertEquals("\n$_ = $proceed($$);", StandaloneExp.PROCEED_AND_ASSIGN.getCode());
	}

	@Test
	public void testAppendStandalone() {
		StandaloneExp standaloneExp = new IfExp(BooleanExp.TRUE).append(new IfExp(BooleanExp.TRUE));
		assertEquals("\nif (true) {\n}\nif (true) {\n}", standaloneExp.getCode());
		assertFalse(standaloneExp.isEmpty());
	}

	@Test
	public void testAppendStandaloneEmpty() {
		StandaloneExp standaloneExp = new EmptyExp().append(new EmptyExp());
		assertEquals("", standaloneExp.getCode());
		assertTrue(standaloneExp.isEmpty());
	}

	@Test
	public void testAppendNested() {
		assertEquals("\nif (true) {\n}\ntrue;", new IfExp(BooleanExp.TRUE).append(BooleanExp.TRUE).getCode());
	}

	@Test
	public void testInsertBeforeWithMethod() throws Exception {
		CtBehavior behavior = mock(CtMethod.class);
		StandaloneExp exp = new IfExp(BooleanExp.TRUE);
		exp.insertBefore(behavior);
		verify(behavior).insertBefore("{ \nif (true) {\n} }");
	}

	@Test
	public void testInsertBeforeWithConstructor() throws Exception {
		CtConstructor behavior = mock(CtConstructor.class);
		when(behavior.isClassInitializer()).thenReturn(Boolean.FALSE);
		StandaloneExp exp = new IfExp(BooleanExp.TRUE);
		exp.insertBefore(behavior);
		verify(behavior).insertBeforeBody("{ \nif (true) {\n} }");
	}

	@Test
	public void testInsertBeforeWithInitializer() throws Exception {
		CtConstructor behavior = mock(CtConstructor.class);
		when(behavior.isClassInitializer()).thenReturn(Boolean.TRUE);
		StandaloneExp exp = new IfExp(BooleanExp.TRUE);
		exp.insertBefore(behavior);
		verify(behavior).insertBefore("{ \nif (true) {\n} }");
	}

	@Test
	public void testInsertAfter() throws Exception {
		CtBehavior behavior = mock(CtBehavior.class);
		StandaloneExp exp = new IfExp(BooleanExp.TRUE);
		exp.insertAfter(behavior);
		verify(behavior).insertAfter("{ \nif (true) {\n} }");
	}

	@Test
	public void testCodeStandaloneExpFromStandalone() {
		StandaloneExp standaloneExp = CodeStandaloneExp.fromStandalone("someCode", false);
		assertEquals("someCode", standaloneExp.getCode());
	}

	@Test
	public void testCodeStandaloneExpFromNested() {
		assertEquals("\nsomeCode;", CodeStandaloneExp.fromNested("someCode").getCode());
	}
}
