package de.vksi.c4j.systemtest.cleanup;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;
import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TestUtil;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractCacheCleanupSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractCacheCleanup() throws Exception {
		TargetClass target = new TargetClass();
		WeakReference<TargetClass> targetWeakReference = new WeakReference<TargetClass>(target);
		target.method(3);
		target = null;
		TestUtil.forceGarbageCollection();
		assertNull(targetWeakReference.get());
	}

	@ContractReference(ContractClass.class)
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

	@ContractReference(OldClassContract.class)
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
