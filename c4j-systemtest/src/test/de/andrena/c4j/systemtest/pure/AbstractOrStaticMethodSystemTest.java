package de.andrena.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.Pure;

public class AbstractOrStaticMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testUnpureAbstractMethod() {
		new TargetClass().unpureMethod();
	}

	@Test
	public void testPureAbstractMethod() {
		new TargetClass().pureMethod();
	}

	public static class TargetClass extends AbstractClass {
		@Override
		public void unpureMethod() {
		}

		@Override
		public void pureMethod() {
		}
	}

	public abstract static class AbstractClass {
		public abstract void unpureMethod();

		@Pure
		public abstract void pureMethod();
	}
}
