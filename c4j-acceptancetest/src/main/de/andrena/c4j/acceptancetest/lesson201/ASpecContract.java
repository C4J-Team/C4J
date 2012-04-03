package de.andrena.c4j.acceptancetest.lesson201;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;

public class ASpecContract implements ASpec {

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query");
		}
		if (post()) {
			System.out.println("Post-Condition query");
		}
		return ignored();
	}

	@Override
	public void command(int wert) {
		if (pre()) {
			System.out.println("Pre-Condition command");
		}
		if (post()) {
			System.out.println("Post-Condition command");
		}
	}

}
