package de.andrena.next.acceptancetest.timeofday;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(RicherTimeOfDaySpecContract.class)
public interface RicherTimeOfDaySpec extends TimeOfDaySpec {
	
	@Pure
	int getNearestHour();

}