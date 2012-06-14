package de.vksi.c4j.acceptancetest.lesson202;

import de.vksi.c4j.ClassInvariant;

public class BContract extends B {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class B");
	}

}
