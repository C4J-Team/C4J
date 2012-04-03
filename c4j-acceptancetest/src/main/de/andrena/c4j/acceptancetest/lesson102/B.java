package de.andrena.c4j.acceptancetest.lesson102;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(BContract.class)
public class B extends A {

	private int b;

	@Pure
	public int queryB() {
		return b;
	}

	public void commandB(int value) {
		b = value;
	}

	@Override
	public int query(int x, int y) {
		int result = 0;
		result = x * y;
		return result;
	}

	@Override
	public void command(int value) {
		b = value;
	}

}
