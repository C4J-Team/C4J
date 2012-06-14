package de.vksi.c4j.acceptancetest.timeofday;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.result;
import de.vksi.c4j.Target;

public class RicherTimeOfDaySpecContract implements RicherTimeOfDaySpec {

	@Target
	private RicherTimeOfDaySpec target;

	@Override
	public int getHour() {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
		return (Integer) ignored();
	}

	@Override
	public int getMinute() {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
		return (Integer) ignored();
	}

	@Override
	public int getSecond() {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
		return (Integer) ignored();
	}

	@Override
	public void setHour(int hour) {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setMinute(int minute) {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setSecond(int second) {
		if (preCondition()) {
			// no further pre-condition identified yet
		}
		if (postCondition()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public int getNearestHour() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			int hour = target.getHour();
			int minute = target.getMinute();
			assert !(minute < 30) || (result == hour) : "if minute < 30 then result == hour";
			assert !(minute >= 30 && hour < 23) || (result == hour + 1) : "if minute >= 30 && hour < 23 then result == hour + 1";
			assert !(minute >= 30 && hour == 23) || (result == 0) : "if minute >= 30 && hour == 23 then result == 23";
		}
		return (Integer) ignored();
	}

}
