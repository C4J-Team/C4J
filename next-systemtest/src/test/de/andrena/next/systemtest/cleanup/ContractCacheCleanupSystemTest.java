package de.andrena.next.systemtest.cleanup;

import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;

import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.Target;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.systemtest.TestUtil;

public class ContractCacheCleanupSystemTest {

	@Test
	public void testContractCacheCleanup() throws Exception {
		TargetClass target = new TargetClass();
		WeakReference<TargetClass> targetWeakReference = new WeakReference<TargetClass>(target);
		target.method(3);
		target = null;
		TestUtil.forceGarbageCollection();
		assertNull(targetWeakReference.get());
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		public void method(int value) {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int value) {
			if (pre()) {
				assert value > 0;
			}
		}
	}

	@Test
	public void testContractCacheCleanupHavingTarget() {
		OldClass target = new OldClass();
		WeakReference<OldClass> targetWeakReference = new WeakReference<OldClass>(target);
		target.method(3);
		target = null;
		TestUtil.forceGarbageCollection();
		assertNull(targetWeakReference.get());
	}

	@Test
	public void testOldStoreCleanup() {
		new OldClass().method(3);
		assertEquals(0, Evaluator.getOldStoreSize());
	}

	@Test
	public void testOldStoreCleanupAfterFailure() {
		try {
			new OldClass().methodFailing(3);
		} catch (AssertionError e) {
		}
		assertEquals(0, Evaluator.getOldStoreSize());
	}

	@Contract(OldClassContract.class)
	public static class OldClass {
		protected int value;

		public void method(int incrementor) {
			value += incrementor;
		}

		public void methodFailing(int incrementor) {
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
		public void methodFailing(int incrementor) {
			if (pre()) {
				assert false;
			}
			if (post()) {
				int oldValue = old(target.value);
				assert target.value == (oldValue + incrementor);
			}
		}
	}
}
