package de.andrena.c4j.acceptancetest.timeofday;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import static de.andrena.c4j.Condition.result;
import de.andrena.c4j.Target;

public class RicherTimeOfDaySpecContract implements RicherTimeOfDaySpec {

	@Target
	private RicherTimeOfDaySpec target;

	@Override
	public int getHour() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public int getMinute() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public int getSecond() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public void setHour(int hour) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setMinute(int minute) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setSecond(int second) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public int getNearestHour() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			int hour = target.getHour();
			int minute = target.getMinute();
			assert !(minute < 30) || (result == hour) : "if minute < 30 then result == hour";
			assert !(minute >= 30 && hour < 23) || (result == hour + 1) : "if minute >= 30 && hour < 23 then result == hour + 1";
			assert !(minute >= 30 && hour == 23) || (result == 0) : "if minute >= 30 && hour == 23 then result == 23";
		}
		return ignored();
	}

}
