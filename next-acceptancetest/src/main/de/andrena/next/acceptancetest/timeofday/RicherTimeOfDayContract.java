package de.andrena.next.acceptancetest.timeofday;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Condition;

public class RicherTimeOfDayContract extends RicherTimeOfDay {
	private RicherTimeOfDay target = Condition.target();

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