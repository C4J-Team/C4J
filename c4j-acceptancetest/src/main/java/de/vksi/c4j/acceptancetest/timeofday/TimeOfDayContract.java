package de.vksi.c4j.acceptancetest.timeofday;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Target;

public class TimeOfDayContract extends TimeOfDay {

	@Target
	private TimeOfDay target;

	public TimeOfDayContract() {
		super();
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			assert target.getHour() == 0 : "hour == 0";
			assert target.getMinute() == 0 : "minute == 0";
			assert target.getSecond() == 0 : "second == 0";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		assert 0 <= target.getHour() && target.getHour() <= 23 : "hour always valid";
		assert 0 <= target.getMinute() && target.getMinute() <= 59 : "minute always valid";
		assert 0 <= target.getSecond() && target.getSecond() <= 59 : "second always valid";
	}

}
