package de.andrena.c4j.acceptancetest.subclasses;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(TopContract.class)
public class Top {

	@Pure
	int pre(String parameter) {
		return 42;
	}

	@Pure
	int post(String parameter) {
		return 42;
	}

	@Pure
	int preAndPost(String parameter) {
		return 42;
	}

	@Pure
	int invariant(String parameter) {
		return 42;
	}

	@Pure
	int unchanged() {
		return 42;
	}

	@Pure
	int old() {
		return 42;
	}
}
