package de.vksi.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.IfExp;

public class IfExpTest {

	@Test
	public void testIfExp() {
		assertEquals("\nif (true) {\n}", new IfExp(BooleanExp.TRUE).getCode());
	}

	@Test
	public void testAddIfBody() {
		IfExp ifExp = new IfExp(BooleanExp.TRUE);
		ifExp.addIfBody(new IfExp(BooleanExp.TRUE));
		assertEquals("\nif (true) {\nif (true) {\n}\n}", ifExp.getCode());
	}

	@Test
	public void testAddElseBody() {
		IfExp ifExp = new IfExp(BooleanExp.TRUE);
		ifExp.addElseBody(new IfExp(BooleanExp.TRUE));
		assertEquals("\nif (true) {\n} else {\nif (true) {\n}\n}", ifExp.getCode());
	}

	@Test
	public void testAddIfAndElseBody() {
		IfExp ifExp = new IfExp(BooleanExp.TRUE);
		ifExp.addIfBody(new IfExp(BooleanExp.TRUE));
		ifExp.addElseBody(new IfExp(BooleanExp.TRUE));
		assertEquals("\nif (true) {\nif (true) {\n}\n} else {\nif (true) {\n}\n}", ifExp.getCode());
	}
}
