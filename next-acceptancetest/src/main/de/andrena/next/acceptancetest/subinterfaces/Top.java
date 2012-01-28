package de.andrena.next.acceptancetest.subinterfaces;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(TopContract.class)
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
