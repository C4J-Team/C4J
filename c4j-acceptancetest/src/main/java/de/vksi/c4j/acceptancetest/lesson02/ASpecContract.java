package de.vksi.c4j.acceptancetest.lesson02;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;

public class ASpecContract implements ASpec {

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query");
		}
		if (post()) {
			System.out.println("Post-Condition query");
		}
		return (Integer) ignored();
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
