package de.andrena.c4j.acceptancetest.timeofday;

import de.andrena.c4j.Contract;
import de.andrena.c4j.Pure;

@Contract(TimeOfDaySpecContract.class)
public interface TimeOfDaySpec {

	@Pure
	int getHour();

	@Pure
	int getMinute();

	@Pure
	int getSecond();

	void setHour(int hour);

	void setMinute(int minute);

	void setSecond(int second);

}
