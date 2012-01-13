package de.andrena.next.acceptancetest.subinterfaces;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

public class TopContract implements Top {

	@Override
	public int a(String parameter) {
		if (pre()) {
			assert parameter != null : "parameter must not be null";
		}
		return ignored();
	}

}
