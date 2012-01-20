package de.andrena.next.acceptancetest.timeofday;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Condition;

public class TimeOfDayContract extends TimeOfDay {
	
	private TimeOfDay target = Condition.target();

	public TimeOfDayContract() {
		super();
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
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
