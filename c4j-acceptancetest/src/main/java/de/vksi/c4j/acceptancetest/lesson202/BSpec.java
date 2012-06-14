package de.vksi.c4j.acceptancetest.lesson202;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.lesson201.ASpec;

@ContractReference(BSpecContract.class)
public interface BSpec extends ASpec {

	@Pure
	int queryB();

	void commandB(int value);

	@Override
	@Pure
	int query(int x, int y);

	@Override
	void command(int value);

}
