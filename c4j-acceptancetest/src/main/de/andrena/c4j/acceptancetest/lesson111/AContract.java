package de.andrena.c4j.acceptancetest.lesson111;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.Target;

public class AContract extends A {

	@Target
	private A target;

	@ClassInvariant
	public void classInvariant() {
		assert a > 0 : "a > 0";
	}

	@Override
	public int query(int x, int y) {
		if (pre()) {
			assert x >= 1 : "x >= 1";
		}
		if (post()) {
			assert y >= 2 : "y >= 2";
		}
		return ignored();
	}

	@Override
	public void command(int value) {
		if (pre()) {
			assert value > 0 : "value > 0";
		}
		if (post()) {
			assert target.getA() == value : "a set";
		}
	}

}
