package de.vksi.c4j.acceptancetest.object;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(ObjectSpecContract.class)
public interface ObjectSpec {

	@Pure
	boolean equals(Object obj);

	@Pure
	int hashCode();

	@Pure
	String toString();

}