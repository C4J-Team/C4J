package de.vksi.c4j.acceptancetest.timeofday;

import de.vksi.c4j.ContractReference;

@ContractReference(TimeOfDayContract.class)
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
		int result = 0;
		result = hour;
		return result;
	}

	@Override
	public int getMinute() {
		int result = 0;
		result = minute;
		return result;
	}

	@Override
	public int getSecond() {
		int result = 0;
		result = second;
		return result;
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
