package de.andrena.next.systemtest.cleanup;

import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;

import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;
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

	// TODO
	@Test
	public void testContractCacheCleanupHavingTarget() {
		TargetClassWithTarget target = new TargetClassWithTarget();
		WeakReference<TargetClassWithTarget> targetWeakReference = new WeakReference<TargetClassWithTarget>(target);
		target.method(3);
		target = null;
		TestUtil.forceGarbageCollection();
		assertNull(targetWeakReference.get());
	}

	@Contract(ContractClassWithTarget.class)
	public static class TargetClassWithTarget {
		protected int value;

		public void method(int incrementor) {
			value += incrementor;
		}
	}

	public static class ContractClassWithTarget extends TargetClassWithTarget {
		private TargetClassWithTarget target = Condition.target();

		@Override
		public void method(int value) {
			if (post()) {
				assert target.value >= 0;
			}
		}
	}

	// TODO
	@Test
	public void testOldStoreCleanup() {
		new OldClass().method(3);
		assertNull(Evaluator.oldFieldAccess("value"));
	}

	@Contract(OldClassContract.class)
	public static class OldClass {
		protected int value;

		public void method(int incrementor) {
			value += incrementor;
		}
	}

	public static class OldClassContract extends OldClass {
		private OldClass target = Condition.target();

		@Override
		public void method(int incrementor) {
			if (post()) {
				int oldValue = old(target.value);
				assert target.value == (oldValue + incrementor);
			}
		}
	}
}
