package de.andrena.c4j.acceptancetest.timeofday;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.Target;

public class RicherTimeOfDayContract extends RicherTimeOfDay {

	@Target
	private RicherTimeOfDay target;

	public RicherTimeOfDayContract() {
		super();
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			assert target.getNearestHour() == 0 : "nearest hour == 0";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		assert 0 <= target.getNearestHour() && target.getNearestHour() <= 23 : "nearest hour always valid";
	}

}