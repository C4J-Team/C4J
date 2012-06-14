package de.vksi.c4j.acceptancetest.lesson203;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.lesson202.BSpec;

@ContractReference(CSpecContract.class)
public interface CSpec extends BSpec {

	@Pure
	int queryC();

	void commandC(int value);

	@Override
	@Pure
	int query(int x, int y);

	@Override
	void command(int value);
}
