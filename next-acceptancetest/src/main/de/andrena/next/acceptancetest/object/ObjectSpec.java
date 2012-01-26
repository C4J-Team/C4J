package de.andrena.next.acceptancetest.object;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(ObjectSpecContract.class)
public interface ObjectSpec {

	@Pure
	boolean equals(Object obj);

	@Pure
	int hashCode();

	@Pure
	String toString();

}