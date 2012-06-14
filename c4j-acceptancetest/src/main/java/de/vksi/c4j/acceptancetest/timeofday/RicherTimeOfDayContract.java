package de.vksi.c4j.acceptancetest.timeofday;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Target;

public class RicherTimeOfDayContract extends RicherTimeOfDay {

	@Target
	private RicherTimeOfDay target;

	public RicherTimeOfDayContract() {
		super();
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			assert target.getNearestHour() == 0 : "nearest hour == 0";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		assert 0 <= target.getNearestHour() && target.getNearestHour() <= 23 : "nearest hour always valid";
	}

}