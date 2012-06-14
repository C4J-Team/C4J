package de.vksi.c4j.acceptancetest.lesson101;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;
import de.vksi.c4j.ClassInvariant;

public class AContract extends A {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class A");
	}

	@Override
	public int query(int x, int y) {
		if (pre()) {
			System.out.println("Pre-Condition query class A");
		}
		if (post()) {
			System.out.println("Post-Condition query class A");
		}
		return (Integer) ignored();
	}

	@Override
	public void command(int value) {
		if (pre()) {
			System.out.println("Pre-Condition command class A");
		}
		if (post()) {
			System.out.println("Post-Condition command class A");
		}
	}

}
