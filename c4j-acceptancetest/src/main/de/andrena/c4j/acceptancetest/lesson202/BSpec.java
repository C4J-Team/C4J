package de.andrena.c4j.acceptancetest.lesson202;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(BSpecContract.class)
public interface BSpec extends ASpec {

	@Pure
	int queryB();

	void commandB(int value);

	@Override
	int query(int x, int y);

	@Override
	void command(int wert);

}
