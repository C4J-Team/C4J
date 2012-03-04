package de.andrena.c4j.acceptancetest.object;

import de.andrena.c4j.Contract;
import de.andrena.c4j.Pure;

@Contract(ObjectSpecContract.class)
public interface ObjectSpec {

	@Pure
	boolean equals(Object obj);

	@Pure
	int hashCode();

	@Pure
	String toString();

}