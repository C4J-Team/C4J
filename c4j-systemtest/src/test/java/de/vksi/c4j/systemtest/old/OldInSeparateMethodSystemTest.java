package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldInSeparateMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testOldInSeparateMethod() {
		dummy.setValue(5);
		dummy.checkInSeparateMethod();
	}

	@Test
	public void testOldInNestedSeparateMethod() {
		dummy.setValue(5);
		dummy.checkInNestedSeparateMethod();
	}

	@ContractReference(DummyContract.class)
	public static class DummyClass {
		protected int value;

		public void setValue(int value) {
			this.value = value;
		}

		public void checkInNestedSeparateMethod() {
			value++;
		}

		@Pure
		public int getValue() {
			return value;
		}

		public void checkInSeparateMethod() {
			value++;
		}
	}

	public static class DummyContract extends DummyClass {
		@Target
		private DummyClass target;

		@Override
		public void checkInSeparateMethod() {
			if (postCondition()) {
				assert target.getValue() == getOldValue() + 1;
			}
		}

		@Override
		public void checkInNestedSeparateMethod() {
			if (postCondition()) {
				assert target.getValue() == getNestedOldValue() + 1;
			}
		}

		private int getNestedOldValue() {
			return getOldValue();
		}

		private int getOldValue() {
			return old(target.getValue());
		}
	}

}
