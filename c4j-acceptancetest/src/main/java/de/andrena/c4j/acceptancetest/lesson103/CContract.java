package de.andrena.c4j.acceptancetest.lesson103;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.ClassInvariant;

public class CContract extends C {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class C");
	}

	@Override
	public int queryC() {
		if (pre()) {
			System.out.println("Pre-Condition queryC");
		}
		if (post()) {
			System.out.println("Post-Condition queryC");
		}
		return ignored();
	}

	@Override
	public void commandC(int value) {
		if (pre()) {
			System.out.println("Pre-Condition commandC");
		}
		if (post()) {
			System.out.println("Post-Condition commandC");
		}
	}

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query class C");
		}
		if (post()) {
			System.out.println("Post-Condition query class C");
		}
		return ignored();
	}

	@Override
	public void command(int value) {
		if (pre()) {
			System.out.println("Pre-Condition command class C");
		}
		if (post()) {
			System.out.println("Post-Condition command class C");
		}
	}

}
