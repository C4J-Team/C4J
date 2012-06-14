package de.vksi.c4j.acceptancetest.lesson202;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

public class BSpecContract implements BSpec {

	@Override
	public int queryB() {
		if (preCondition()) {
			System.out.println("Pre-Condition queryB");
		}
		if (postCondition()) {
			System.out.println("Post-Condition queryB");
		}
		return (Integer) ignored();
	}

	@Override
	public void commandB(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition commandB");
		}
		if (postCondition()) {
			System.out.println("Post-Condition commandB");
		}
	}

	@Override
	public int query(int x, int y) {
		if (preCondition()) {
			System.out.println("Pre-Condition query interface BSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query interface BSpec");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition command interface BSpec");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command interface BSpec");
		}
	}

}
