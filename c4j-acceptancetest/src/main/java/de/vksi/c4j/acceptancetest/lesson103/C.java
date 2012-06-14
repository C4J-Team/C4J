package de.vksi.c4j.acceptancetest.lesson103;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.lesson102.B;

@ContractReference(CContract.class)
public class C extends B {

	private int c;

	@Pure
	public int queryC() {
		return c;
	}

	public void commandC(int value) {
		c = value;
	}

	@Override
	@Pure
	public int query(int x, int y) {
		int result = super.query(x, y);
		result = result + x - y;
		return result;
	}

	@Override
	public void command(int value) {
		super.command(value);
		c = value;
	}

}
