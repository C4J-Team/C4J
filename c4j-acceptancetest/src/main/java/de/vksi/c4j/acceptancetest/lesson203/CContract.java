package de.vksi.c4j.acceptancetest.lesson203;

import de.vksi.c4j.ClassInvariant;

public class CContract extends C {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class C");
	}

}
