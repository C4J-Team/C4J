package de.vksi.c4j.systemtest.classinvariant;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ClassInvariantWithStaticMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Test
	public void testInvariantWithMethod() {
		dummy = new DummyClass();
		dummy.setHour(23);
	}

	@Test(expected = AssertionError.class)
	public void testInvariantWithMethodFailing() {
		dummy = new DummyClass();
		dummy.setHour(-1);
	}

	@SuppressWarnings("unused")
	@ContractReference(DummyClassContract.class)
	private static class DummyClass {
		private int hour;

		public static void staticMethod() {
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

		@ClassInvariant
		public void invariant() {
			assert target.getHour() >= 0 && target.getHour() <= 23;
		}
	}
}
