package de.andrena.c4j.acceptancetest.lesson102;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.acceptancetest.lesson101.A;

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
		int result = super.query(x, y);
		result = result + x * y;
		return result;
	}

	@Override
	public void command(int value) {
		super.command(value);
		b = value;
	}

}
