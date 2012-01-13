package de.andrena.next.acceptancetest.subinterfaces;

public class VeryBottom implements Bottom {

	private final int value;

	public VeryBottom(int value) {
		this.value = value;
	}

	@Override
	public int a(String parameter) {
		return value;
	}

	@Override
	public String b(int p) {
		return "";
	}

}
