package de.vksi.c4j.acceptancetest.lesson203;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

public class CSpecContract implements CSpec {

	@Override
	public int queryC() {
		if (preCondition()) {
			System.out.println("Pre-Condition queryC");
		}
		if (postCondition()) {
			System.out.println("Post-Condition queryC");
		}
		return (Integer) ignored();
	}

	@Override
	public void commandC(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition commandC");
		}
		if (postCondition()) {
			System.out.println("Post-Condition commandC");
		}
	}

	@Override
	public int query(int x, int y) {
		if (preCondition()) {
			System.out.println("Pre-Condition query interface CSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query interface CSpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition command interface CSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command interface CSpec");
		}
	}

	@Override
	public int queryB() {
		if (preCondition()) {
			System.out.println("Pre-Condition queryB interface CSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition queryB interface CSpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void commandB(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition commandB interface CSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition commandB interface CSpec");
		}
	}

}
