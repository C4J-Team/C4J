package de.andrena.next.acceptancetest.timeofday;

import de.andrena.next.Contract;

@Contract(TimeOfDayContract.class)
public class TimeOfDay implements TimeOfDaySpec {
	private int hour;
	private int minute;
	private int second;

	public TimeOfDay() {
		hour = 0;
		minute = 0;
		second = 0;
	}

	@Override
	public int getHour() {
		return hour;
	}

	@Override
	public int getMinute() {
		return minute;
	}

	@Override
	public int getSecond() {
		return second;
	}

	@Override
	public void setHour(int hour) {
		this.hour = hour;
	}

	@Override
	public void setMinute(int minute) {
		this.minute = minute;
	}

	@Override
	public void setSecond(int second) {
		this.second = second;
	}
}
