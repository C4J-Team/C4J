package de.andrena.c4j.acceptancetest.lesson102;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.ClassInvariant;

public class BContract extends B {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class B");
	}

	@Override
	public int queryB() {
		if (pre()) {
			System.out.println("Pre-Condition queryB");
		}
		if (post()) {
			System.out.println("Post-Condition queryB");
		}
		return ignored();
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
			System.out.println("Pre-Condition query class B");
		}
		if (post()) {
			System.out.println("Post-Condition query class B");
		}
		return ignored();
	}

}
