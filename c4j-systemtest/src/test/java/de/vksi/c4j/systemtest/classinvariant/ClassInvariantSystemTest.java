package de.vksi.c4j.systemtest.classinvariant;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
	private static class DummyClass {
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

	@SuppressWarnings("unused")
	private static class DummyClassContract extends DummyClass {
		@Target
		private DummyClass target;

		public DummyClassContract() {
			super(0);
		}

		@ClassInvariant
		public void invariant() {
			assert target.getHour() >= 0 && target.getHour() <= 23;
		}
	}
}
