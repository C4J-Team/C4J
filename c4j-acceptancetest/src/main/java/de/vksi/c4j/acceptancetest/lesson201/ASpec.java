package de.vksi.c4j.acceptancetest.lesson201;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(ASpecContract.class)
public interface ASpec {

	@Pure
	int query(int x, int y);

	void command(int value);

}
