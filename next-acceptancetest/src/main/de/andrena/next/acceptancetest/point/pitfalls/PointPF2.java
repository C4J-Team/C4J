package de.andrena.next.acceptancetest.point.pitfalls;

import de.andrena.next.Pure;
import de.andrena.next.acceptancetest.object.ObjectSpec;

public class PointPF2 implements ObjectSpec {

	private final int x;
	private final int y;

	public PointPF2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Pure
	public int getX() {
		return x;
	}

	@Pure
	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		PointPF2 other = null;
		if (obj instanceof PointPF2) {
			other = (PointPF2) obj;
			result = this.getX() == other.getX() && this.getY() == other.getY();
		}
		return result;
	}

}
