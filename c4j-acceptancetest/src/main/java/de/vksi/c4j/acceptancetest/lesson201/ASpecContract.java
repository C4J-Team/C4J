package de.vksi.c4j.acceptancetest.lesson201;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;

public class ASpecContract implements ASpec {

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query interface ASpec");
		}
		if (post()) {
			System.out.println("Post-Condition query interface ASpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int wert) {
		if (pre()) {
			System.out.println("Pre-Condition command interface ASpec");
		}
		if (post()) {
			System.out.println("Post-Condition command interface ASpec");
		}
	}

}
