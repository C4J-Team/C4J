package de.andrena.next.acceptancetest.subinterfaces;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static de.andrena.next.Condition.result;

public class TopContract implements Top {

	@Override
	public int a(String parameter) {
		if (pre()) {
			assert parameter != null : "parameter must not be null";
		}
		if (post()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return ignored();
	}

}
