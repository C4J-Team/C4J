package de.vksi.c4j.acceptancetest.timeofday;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(RicherTimeOfDaySpecContract.class)
public interface RicherTimeOfDaySpec extends TimeOfDaySpec {
	
	@Pure
	int getNearestHour();

}