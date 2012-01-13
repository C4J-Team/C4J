package de.andrena.next.acceptancetest.subclasses;

import de.andrena.next.Contract;

@Contract(TopContract.class)
public class Top {

	int pre(String parameter) {
		return 42;
	}

	int post(String parameter) {
		return 42;
	}

	int preAndPost(String parameter) {
		return 42;
	}

	int invariant(String parameter) {
		return 42;
	}

	int unchanged() {
		return 42;
	}

	int old() {
		return 42;
	}

}
