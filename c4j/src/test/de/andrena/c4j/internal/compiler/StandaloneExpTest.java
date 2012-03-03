package de.andrena.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.junit.Test;

import de.andrena.c4j.internal.compiler.BooleanExp;
import de.andrena.c4j.internal.compiler.IfExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StandaloneExp.CodeStandaloneExp;

public class StandaloneExpTest {

	@Test
	public void testProceed() {
		assertEquals("\n$_ = $proceed($$);", StandaloneExp.proceed.getCode());
	}

	@Test
	public void testAppendStandalone() {
		assertEquals("\nif (true) {\n}\nif (true) {\n}", new IfExp(BooleanExp.TRUE).append(new IfExp(BooleanExp.TRUE))
				.getCode());
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
		assertEquals("someCode", CodeStandaloneExp.fromStandalone("someCode").getCode());
	}

	@Test
	public void testCodeStandaloneExpFromNested() {
		assertEquals("\nsomeCode;", CodeStandaloneExp.fromNested("someCode").getCode());
	}
}
