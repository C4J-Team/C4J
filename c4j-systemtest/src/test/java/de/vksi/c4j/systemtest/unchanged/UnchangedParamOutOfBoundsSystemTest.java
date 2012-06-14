package de.vksi.c4j.systemtest.unchanged;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.unchanged;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
			if (preCondition()) {
				assert hour >= 0 && hour <= 23;
			}
			if (postCondition()) {
				assert hour == target.getHour();
				assert unchanged(target.getMinute());
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
