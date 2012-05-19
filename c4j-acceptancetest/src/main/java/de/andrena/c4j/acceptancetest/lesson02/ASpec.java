package de.andrena.c4j.acceptancetest.lesson02;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(ASpecContract.class)
public interface ASpec {

	@Pure
	int query(int x, int y);

	void command(int wert);

}
