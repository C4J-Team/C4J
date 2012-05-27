package de.andrena.c4j.acceptancetest.lesson202;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;

public class BSpecContract implements BSpec {

	@Override
	public int queryB() {
		if (pre()) {
			System.out.println("Pre-Condition queryB");
		}
		if (post()) {
			System.out.println("Post-Condition queryB");
		}
		return (Integer) ignored();
	}

	@Override
	public void commandB(int value) {
		if (pre()) {
			System.out.println("Pre-Condition commandB");
		}
		if (post()) {
			System.out.println("Post-Condition commandB");
		}
	}

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query interface BSpec");
		}
		if (post()) {
			System.out.println("Post-Condition query interface BSpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (pre()) {
			System.out.println("Pre-Condition command interface BSpec");
		}
		if (post()) {
			System.out.println("Post-Condition command interface BSpec");
		}
	}

}
