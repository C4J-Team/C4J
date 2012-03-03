package de.andrena.c4j.systemtest.cleanup;

import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.Contract;
import de.andrena.next.Target;
import de.andrena.next.internal.evaluator.Evaluator;

public class OldStoreCleanupSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testOldStoreCleanup() {
		new OldClass().method(3);
		assertEquals(0, Evaluator.getOldStoreSize());
	}

	@Test
	public void testOldStoreCleanupAfterFailure() {
		try {
			new OldClass().methodFailingContract(3);
		} catch (AssertionError e) {
		}
		assertEquals(0, Evaluator.getOldStoreSize());
	}

	@Test
	public void testOldStoreCleanupAfterFailureInMethod() {
		try {
			new OldClass().methodFailingSelf(3);
		} catch (RuntimeException e) {
		}
		assertEquals(0, Evaluator.getOldStoreSize());
	}

	@Contract(OldClassContract.class)
	public static class OldClass {
		protected int value;

		public void method(int incrementor) {
			value += incrementor;
		}

		public void methodFailingContract(int incrementor) {
		}

		public void methodFailingSelf(int incrementor) {
			throw new RuntimeException();
		}
	}

	public static class OldClassContract extends OldClass {
		@Target
		private OldClass target;

		@Override
		public void method(int incrementor) {
			if (post()) {
				int oldValue = old(target.value);
				assert target.value == (oldValue + incrementor);
			}
		}

		@Override
		public void methodFailingContract(int incrementor) {
			if (pre()) {
				assert false;
			}
			if (post()) {
				assert target.value == old(target.value).intValue() + incrementor;
			}
		}

		@Override
		public void methodFailingSelf(int incrementor) {
			if (post()) {
				assert target.value == old(target.value);
			}
		}
	}

	@Test
	public void testOldStoreCleanupWithMultipleCalls() {
		new TargetClass().method();
	}

	@Contract(TargetClassContract.class)
	public static class TargetClass extends SuperClass {
		@Override
		public void method() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (post()) {
				assert old(target.field) == 1;
			}
		}
	}

	@Contract(SuperClassContract.class)
	public static class SuperClass {
		protected int field = 1;

		public void method() {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Target
		private SuperClass target;

		@Override
		public void method() {
			if (post()) {
				assert old(target.field) == 1;
			}
		}
	}
}
