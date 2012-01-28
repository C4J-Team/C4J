package de.andrena.next.systemtest.old;

import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.systemtest.TransformerAwareRule;

public class OldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testOldWithField() {
		dummy.setValue(5);
		dummy.incrementValueCheckField();
	}

	@Test
	public void testOldWithMethod() {
		dummy.setValue(5);
		dummy.incrementValueCheckMethod();
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected int value;
		protected OtherClass otherValue;

		public void setValue(int value) {
			this.value = value;
		}

		@Pure
		public int getValue() {
			return value;
		}

		public void incrementValueCheckField() {
			value++;
		}

		public void incrementValueCheckMethod() {
			value++;
		}
	}

	public static class OtherClass {
		public InputStream stream;

		public int otherMethod() {
			return 0;
		}
	}

	public static class DummyContract extends DummyClass {
		private DummyClass target = Condition.target();

		@Override
		public void incrementValueCheckField() {
			if (post()) {
				assert target.value == old(target.value) + 1;
			}
		}

		@Override
		public void incrementValueCheckMethod() {
			if (post()) {
				assert target.getValue() == old(target.getValue()) + 1;
			}
		}
	}
}
