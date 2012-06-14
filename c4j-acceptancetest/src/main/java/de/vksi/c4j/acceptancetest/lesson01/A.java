package de.vksi.c4j.acceptancetest.lesson01;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(AContract.class)
public class A {

	private int wert;

	@Pure
	public int query(int x, int y) {
		int result = 0;
		result = x + y;
		return result;
	}

	public void command(int wert) {
		this.wert = wert;
	}

}
