package de.vksi.c4j.acceptancetest.lesson201;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

public class ASpecContract implements ASpec {

	@Override
	public int query(int x, int y) {
		if (preCondition()) {
			System.out.println("Pre-Condition query interface ASpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query interface ASpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int wert) {
		if (preCondition()) {
			System.out.println("Pre-Condition command interface ASpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command interface ASpec");
		}
	}

}
