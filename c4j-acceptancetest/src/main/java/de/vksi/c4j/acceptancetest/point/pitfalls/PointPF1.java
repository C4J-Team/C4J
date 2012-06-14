package de.vksi.c4j.acceptancetest.point.pitfalls;

import de.vksi.c4j.acceptancetest.object.ObjectSpec;

public class PointPF1 implements ObjectSpec {

	private final int x;
	private final int y;

	public PointPF1(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	// @Override
	// --> reveals the utterly wrong definition of equals
	public boolean equals(PointPF1 obj) {
		boolean result = false;
		result = this.getX() == obj.getX() && this.getY() == obj.getY();
		return result;
	}

}
