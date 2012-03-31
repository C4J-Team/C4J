package de.andrena.c4j.systemtest.unchanged;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import static de.andrena.c4j.Condition.unchanged;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class UnchangedParamOutOfBoundsSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testGetHour() throws Exception {
		TargetClass timeOfDay = new TargetClass();
		assertEquals(0, timeOfDay.getHour());
		timeOfDay.setHour(5);
		assertEquals(5, timeOfDay.getHour());
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void setHour(int hour) {
			if (pre()) {
				assert hour >= 0 && hour <= 23;
			}
			if (post()) {
				Object a = null;
				assert hour == target.getHour();
				assert unchanged(a, target.getMinute());
			}
		}
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private int hour;
		private int minute;

		@Pure
		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}

		@Pure
		public int getMinute() {
			return minute;
		}
	}
}
