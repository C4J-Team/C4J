package de.andrena.next.acceptancetest.timeofday;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

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
