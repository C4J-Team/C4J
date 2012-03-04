package de.andrena.c4j.acceptancetest.timeofday;

import de.andrena.c4j.Contract;

@Contract(RicherTimeOfDayContract.class)
public class RicherTimeOfDay extends TimeOfDay implements RicherTimeOfDaySpec {
	
	@Override
	public int getNearestHour() {
		int result = 0;
		int hour = getHour();
		int minute = getMinute();
		if (minute < 30) {
			result = hour;
		} else {
			if (hour < 23) {
				result = hour + 1;
			} else {
				result = 0;
			}
		}
		return result;
	}
	
}
