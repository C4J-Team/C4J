package de.andrena.next.systemtest.unchanged;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.unchanged;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;

public class UnchangedSystemTest {

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

	@Contract(ContractClass.class)
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

		public int getMinute() {
			return minuteForGetter;
		}
	}

	public static class ContractClass extends TargetClass {
		private TargetClass target = Condition.target();

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
