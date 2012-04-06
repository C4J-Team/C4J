package de.andrena.c4j.acceptancetest.lesson202;

import de.andrena.c4j.ClassInvariant;

public class BContract extends B {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class B");
	}

}
