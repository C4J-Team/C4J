package de.vksi.c4j.acceptancetest.lesson02;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

public class ASpecContract implements ASpec {

	@Override
	public int query(int x, int y) {
		if (preCondition()) {
			System.out.println("Pre-Condition query");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int wert) {
		if (preCondition()) {
			System.out.println("Pre-Condition command");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command");
		}
	}

}
