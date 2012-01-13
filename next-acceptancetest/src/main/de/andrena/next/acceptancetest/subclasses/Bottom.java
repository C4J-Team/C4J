package de.andrena.next.acceptancetest.subclasses;

public class Bottom extends Top {

	protected int value;

	public Bottom(int value) {
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
	public int old() {
		return value;
	}

	public String b(int p) {
		return "";
	}
	
}
