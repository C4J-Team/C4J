package de.vksi.c4j.acceptancetest.timeofday;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(TimeOfDaySpecContract.class)
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
