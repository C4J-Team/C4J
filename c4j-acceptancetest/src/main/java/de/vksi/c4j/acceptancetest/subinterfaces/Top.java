package de.vksi.c4j.acceptancetest.subinterfaces;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(TopContract.class)
public interface Top {

	@Pure
	int pre(String parameter);

	@Pure
	int post(String parameter);

	@Pure
	int preAndPost(String parameter);

	@Pure
	int invariant(String parameter);

	@Pure
	int unchanged();

	@Pure
	int old();

}
