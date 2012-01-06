package de.andrena.next.acceptancetest.timeofday;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import de.andrena.next.ClassInvariant;

public class TimeOfDayContract extends TimeOfDay {

	public TimeOfDayContract() {
		super();
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			assert getHour() == 0 : "hour == 0";
			assert getMinute() == 0 : "minute == 0";
			assert getSecond() == 0 : "second == 0";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		assert 0 <= getHour() && getHour() <= 23 : "hour always valid";
		assert 0 <= getMinute() && getMinute() <= 59 : "minute always valid";
		assert 0 <= getSecond() && getSecond() <= 59 : "second always valid";
	}

}
