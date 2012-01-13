package de.andrena.next.acceptancetest.subinterfaces;

public class VeryBottom implements Bottom {

	protected int value;

	public VeryBottom(int value) {
		this.value = value;
	}

	@Override
	public int pre(String parameter) {
		return value;
	}

	@Override
	public int post(String parameter) {
		return value;
	}

	@Override
	public int preAndPost(String parameter) {
		return value;
	}

	@Override
	public int invariant(String parameter) {
		return value;
	}

	@Override
	public int unchanged() {
		return value;
	}

	@Override
	public String b(int p) {
		return "";
	}

}
