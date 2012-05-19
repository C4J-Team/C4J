package de.andrena.c4j.systemtest.unchanged;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.unchanged;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;

public class UnchangedForPrimitivesSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testUnchanged() {
		target.setHour(3);
	}

	@Test(expected = AssertionError.class)
	public void testUnchangedWrongForField() {
		target.setHourWrongForField(3);
	}

	@Test(expected = AssertionError.class)
	public void testUnchangedWrongForMethod() {
		target.setHourWrongForMethod(3);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int hour;
		protected int minute;
		private int minuteForGetter;

		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}

		public void setHourWrongForField(int hour) {
			this.minute = hour;
		}

		public void setHourWrongForMethod(int hour) {
			this.minuteForGetter = hour;
		}

		@Pure
		public int getMinute() {
			return minuteForGetter;
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void setHour(int hour) {
			if (post()) {
				assert unchanged(target.minute);
				assert unchanged(target.getMinute());
			}
		}

		@Override
		public void setHourWrongForField(int hour) {
			if (post()) {
				assert unchanged(target.minute);
			}
		}

		@Override
		public void setHourWrongForMethod(int hour) {
			if (post()) {
				assert unchanged(target.getMinute());
			}
		}
	}
}
