package de.andrena.c4j.systemtest.classinvariant;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;

public class ClassInvariantSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Test
	public void testInvariantWithConstructor() {
		dummy = new DummyClass(0);
	}

	@Test(expected = AssertionError.class)
	public void testInvariantWithConstructorFailing() {
		dummy = new DummyClass(25);
	}

	@Test
	public void testInvariantWithMethod() {
		dummy = new DummyClass(0);
		dummy.setHour(23);
	}

	@Test(expected = AssertionError.class)
	public void testInvariantWithMethodFailing() {
		dummy = new DummyClass(0);
		dummy.setHour(-1);
	}

	@ContractReference(DummyClassContract.class)
	public static class DummyClass {
		private int hour;

		public DummyClass(int hour) {
			this.hour = hour;
		}

		@Pure
		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}
	}

	public static class DummyClassContract extends DummyClass {
		@Target
		private DummyClass target;

		public DummyClassContract(int hour) {
			super(hour);
		}

		@ClassInvariant
		public void invariant() {
			assert target.getHour() >= 0 && target.getHour() <= 23;
		}
	}
}
