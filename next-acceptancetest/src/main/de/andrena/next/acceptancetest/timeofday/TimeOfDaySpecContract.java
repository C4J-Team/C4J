package de.andrena.next.acceptancetest.timeofday;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static de.andrena.next.Condition.result;
import static de.andrena.next.Condition.unchanged;

public class TimeOfDaySpecContract implements TimeOfDaySpec {
	@Override
	public int getHour() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 23 : "result <= 23";
		}
		return ignored();
	}

	@Override
	public int getMinute() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 59 : "result <= 59";
		}
		return ignored();
	}

	@Override
	public int getSecond() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 59 : "result <= 59";
		}
		return ignored();
	}

	@Override
	public void setHour(int hour) {
		if (pre()) {
			assert hour >= 0 : "hour >= 0";
			assert hour <= 23 : "hour <= 23";
		}
		if (post()) {
			assert getHour() == hour : "hour set";
			// TODO unchanged must return a boolean
			unchanged(getMinute());
			unchanged(getSecond());
		}
	}

	@Override
	public void setMinute(int minute) {
		if (pre()) {
			assert minute >= 0 : "minute >= 0";
			assert minute <= 59 : "minute <= 59";
		}
		if (post()) {
			unchanged(getHour());
			assert getMinute() == minute : "minute set";
			unchanged(getSecond());
		}
	}

	@Override
	public void setSecond(int second) {
		if (pre()) {
			assert second >= 0 : "second >= 0";
			assert second <= 59 : "second <= 59";
		}
		if (post()) {
			unchanged(getHour());
			unchanged(getMinute());
			assert getSecond() == second : "second set";
		}
	}

}
