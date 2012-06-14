package de.vksi.c4j.acceptancetest.lesson111;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Target;

public class AContract extends A {

	@Target
	private A target;

	@ClassInvariant
	public void classInvariant() {
		assert a > 0 : "a > 0";
	}

	@Override
	public int query(int x, int y) {
		if (preCondition()) {
			assert x >= 1 : "x >= 1";
		}
		if (postCondition()) {
			assert y >= 2 : "y >= 2";
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (preCondition()) {
			assert value > 0 : "value > 0";
		}
		if (postCondition()) {
			assert target.getA() == value : "a set";
		}
	}

}
