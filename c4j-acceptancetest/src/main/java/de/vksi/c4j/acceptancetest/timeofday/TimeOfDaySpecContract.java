package de.vksi.c4j.acceptancetest.timeofday;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.result;
import static de.vksi.c4j.Condition.unchanged;
import de.vksi.c4j.Target;

public class TimeOfDaySpecContract implements TimeOfDaySpec {

	@Target
	private TimeOfDaySpec target;

	@Override
	public int getHour() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 23 : "result <= 23";
		}
		return (Integer) ignored();
	}

	@Override
	public int getMinute() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 59 : "result <= 59";
		}
		return (Integer) ignored();
	}

	@Override
	public int getSecond() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= 59 : "result <= 59";
		}
		return (Integer) ignored();
	}

	@Override
	public void setHour(int hour) {
		if (preCondition()) {
			assert hour >= 0 : "hour >= 0";
			assert hour <= 23 : "hour <= 23";
			System.out.println(target.getMinute());
		}
		if (postCondition()) {
			assert target.getHour() == hour : "hour set";
			System.out.println(target.getMinute());
			assert unchanged(target.getMinute()) : "minute unchanged";
			assert unchanged(target.getSecond()) : "second unchanged";
		}
	}

	@Override
	public void setMinute(int minute) {
		if (preCondition()) {
			assert minute >= 0 : "minute >= 0";
			assert minute <= 59 : "minute <= 59";
		}
		if (postCondition()) {
			assert unchanged(target.getHour()) : "hour unchanged";
			assert target.getMinute() == minute : "minute set";
			assert unchanged(target.getSecond()) : "second unchanged";
		}
	}

	@Override
	public void setSecond(int second) {
		if (preCondition()) {
			assert second >= 0 : "second >= 0";
			assert second <= 59 : "second <= 59";
		}
		if (postCondition()) {
			assert unchanged(target.getHour()) : "hour unchanged";
			assert unchanged(target.getMinute()) : "minute unchanged";
			assert target.getSecond() == second : "second set";
		}
	}

}
