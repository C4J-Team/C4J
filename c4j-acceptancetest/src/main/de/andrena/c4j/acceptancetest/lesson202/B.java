package de.andrena.c4j.acceptancetest.lesson202;

public class B implements BSpec {

	private int b;

	@Override
	public int queryB() {
		return b;
	}

	@Override
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
