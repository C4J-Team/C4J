package de.vksi.c4j.acceptancetest.lesson103;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;

public class CContract extends C {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class C");
	}

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
			System.out.println("Pre-Condition query class C");
		}
		if (postCondition()) {
			System.out.println("Post-Condition query class C");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (preCondition()) {
			System.out.println("Pre-Condition command class C");
		}
		if (postCondition()) {
			System.out.println("Post-Condition command class C");
		}
	}

}
