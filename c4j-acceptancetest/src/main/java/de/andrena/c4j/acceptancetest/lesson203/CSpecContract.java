package de.andrena.c4j.acceptancetest.lesson203;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;

public class CSpecContract implements CSpec {

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
			System.out.println("Pre-Condition query interface CSpec");
		}
		if (post()) {
			System.out.println("Post-Condition query interface CSpec");
		}
		return ignored();
	}

	@Override
	public void command(int value) {
		if (pre()) {
			System.out.println("Pre-Condition command interface CSpec");
		}
		if (post()) {
			System.out.println("Post-Condition command interface CSpec");
		}
	}

	@Override
	public int queryB() {
		if (pre()) {
			System.out.println("Pre-Condition queryB interface CSpec");
		}
		if (post()) {
			System.out.println("Post-Condition queryB interface CSpec");
		}
		return ignored();
	}

	@Override
	public void commandB(int value) {
		if (pre()) {
			System.out.println("Pre-Condition commandB interface CSpec");
		}
		if (post()) {
			System.out.println("Post-Condition commandB interface CSpec");
		}
	}

}
