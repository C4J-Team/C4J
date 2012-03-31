package de.andrena.c4j.acceptancetest.timeofday;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(RicherTimeOfDaySpecContract.class)
public interface RicherTimeOfDaySpec extends TimeOfDaySpec {
	
	@Pure
	int getNearestHour();

}