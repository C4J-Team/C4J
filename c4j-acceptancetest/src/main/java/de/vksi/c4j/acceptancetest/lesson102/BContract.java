package de.vksi.c4j.acceptancetest.lesson102;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;

public class BContract extends B {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class B");
	}

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
			System.out.println("Pre-Condition query class B");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query class B");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition command class B");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command class B");
		}
	}

}
