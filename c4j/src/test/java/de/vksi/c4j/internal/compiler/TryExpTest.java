package de.vksi.c4j.internal.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.vksi.c4j.internal.compiler.BooleanExp;
import de.vksi.c4j.internal.compiler.IfExp;
import de.vksi.c4j.internal.compiler.TryExp;

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

	@Test
	public void testAddCatch() {
		TryExp exp = new TryExp(new IfExp(BooleanExp.TRUE));
		exp.addCatch(Throwable.class, new IfExp(BooleanExp.TRUE));
		assertEquals("\ntry {\nif (true) {\n}\n} catch (java.lang.Throwable e1) {\nif (true) {\n}\n}", exp.getCode());
	}

	@Test
	public void testAddMultipleCatch() {
		TryExp exp = new TryExp(new IfExp(BooleanExp.TRUE));
		exp.addCatch(AssertionError.class, new IfExp(BooleanExp.TRUE));
		exp.addCatch(Throwable.class, new IfExp(BooleanExp.TRUE));
		assertEquals(
				"\ntry {\nif (true) {\n}\n} catch (java.lang.AssertionError e1) {\nif (true) {\n}\n} catch (java.lang.Throwable e2) {\nif (true) {\n}\n}",
				exp.getCode());
	}
}
