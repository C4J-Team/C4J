package de.vksi.c4j.acceptancetest.lesson02;

import de.vksi.c4j.ContractReference;

@ContractReference(AContract.class)
public class A implements ASpec {

	private int wert;

	public int query(int x, int y) {
		int result = 0;
		result = x + y;
		return result;
	}

	public void command(int wert) {
		this.wert = wert;
	}

}
