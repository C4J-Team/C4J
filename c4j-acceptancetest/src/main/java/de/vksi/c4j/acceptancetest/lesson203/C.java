package de.vksi.c4j.acceptancetest.lesson203;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.acceptancetest.lesson103.CContract;

@ContractReference(CContract.class)
public class C implements CSpec {

	private int c;

	@Override
	public int queryC() {
		return c;
	}

	@Override
	public void commandC(int value) {
		c = value;
	}

	@Override
	public int query(int x, int y) {
		int result = 0;
		result = x - y;
		return result;
	}

	@Override
	public void command(int value) {
		c = value;
	}

	@Override
	public int queryB() {
		return 0;
	}

	@Override
	public void commandB(int value) {
		// do nothing
	}
}
