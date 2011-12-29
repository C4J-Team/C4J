package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TryExpTest {
	@Test
	public void testTryExp() {
		assertEquals("\ntry {\nif (true) {\n}\n}", new TryExp(new IfExp(BooleanExp.TRUE)).getCode());
	}

	@Test
	public void testAddFinally() {
		TryExp exp = new TryExp(new IfExp(BooleanExp.TRUE));
		exp.addFinally(new IfExp(BooleanExp.TRUE));
		assertEquals("\ntry {\nif (true) {\n}\n} finally {\nif (true) {\n}\n}", exp.getCode());
	}
}
