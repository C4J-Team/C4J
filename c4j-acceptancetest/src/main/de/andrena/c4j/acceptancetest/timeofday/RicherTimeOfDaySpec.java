package de.andrena.c4j.acceptancetest.timeofday;

import de.andrena.c4j.Contract;
import de.andrena.c4j.Pure;

@Contract(RicherTimeOfDaySpecContract.class)
public interface RicherTimeOfDaySpec extends TimeOfDaySpec {
	
	@Pure
	int getNearestHour();

}