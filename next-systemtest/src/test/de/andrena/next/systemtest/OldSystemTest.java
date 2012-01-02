package de.andrena.next.systemtest;

import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;

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

	@Test
	public void testOldWithOverriddenMethod() {
		System.out.println("11");
		dummy.setValue(5);
		System.out.println("22");
		dummy.incrementValueCheckOverriddenMethod();
		System.out.println("33");
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected int value;
		protected OtherClass otherValue;

		public void setValue(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public int getValueOverridden() {
			System.out.println("value is: " + value);
			return value;
		}

		public void incrementValueCheckField() {
			value++;
		}

		public void incrementValueCheckMethod() {
			value++;
		}

		public void incrementValueCheckOverriddenMethod() {
			System.out.println("before: " + value);
			value++;
			System.out.println("after: " + value);
		}
	}

	public static class OtherClass {
		public InputStream stream;

		public int otherMethod() {
			return 0;
		}
	}

	public static class DummyContract extends DummyClass {

		@Override
		public void incrementValueCheckField() {
			if (post()) {
				assert value == old(value) + 1;
			}
		}

		@Override
		public void incrementValueCheckMethod() {
			if (post()) {
				System.out.println("new: " + getValue() + " old: " + old(getValue()));
				assert getValue() == old(getValue()) + 1;
			}
		}

		@Override
		public void incrementValueCheckOverriddenMethod() {
			if (post()) {
				System.out.println("new: " + getValueOverridden() + " old: " + old(getValueOverridden()));
				assert getValueOverridden() == old(getValueOverridden()) + 1;
			}
		}

		@Override
		public int getValueOverridden() {
			System.out.println("3");
			return 0;
		}
	}
}
