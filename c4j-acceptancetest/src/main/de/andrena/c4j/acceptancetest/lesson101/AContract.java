package de.andrena.c4j.acceptancetest.lesson101;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.ClassInvariant;

public class AContract extends A {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class A");
	}

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
