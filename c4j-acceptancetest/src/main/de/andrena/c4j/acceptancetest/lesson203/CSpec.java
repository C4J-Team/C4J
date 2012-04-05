package de.andrena.c4j.acceptancetest.lesson203;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.acceptancetest.lesson202.BSpec;

@ContractReference(CSpecContract.class)
public interface CSpec extends BSpec {

	@Pure
	int queryC();

	void commandC(int value);

	@Override
	int query(int x, int y);

	@Override
	void command(int value);
}
