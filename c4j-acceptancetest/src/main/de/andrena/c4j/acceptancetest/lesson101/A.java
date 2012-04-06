package de.andrena.c4j.acceptancetest.lesson101;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(AContract.class)
public class A {

	private int a;

	@Pure
	public int query(int x, int y) {
		int result = 0;
		result = x + y;
		return result;
	}

	public void command(int value) {
		a = value;
	}

}
