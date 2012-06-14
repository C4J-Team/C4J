package de.vksi.c4j.acceptancetest.lesson02;

import de.vksi.c4j.ClassInvariant;

public class AContract extends A {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class A");
	}

}
