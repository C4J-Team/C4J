package de.vksi.c4j.acceptancetest.lesson111;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(AContract.class)
public class A {

	protected int a;

	public A() {
		a = 1;
	}

	@Pure
	public int query(int x, int y) {
		int result = 0;
		result = x + y;
		return result;
	}

	public void command(int value) {
		a = value;
	}

	@Pure
	public int getA() {
		int result = 0;
		result = a;
		return result;
	}

}
