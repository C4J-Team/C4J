package de.vksi.c4j.acceptancetest.point.pitfalls;

import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.object.ObjectSpec;

public class PointPF3 implements ObjectSpec {

	private int x;
	private int y;

	public PointPF3(int x, int y) {
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

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		PointPF3 other = null;
		if (obj instanceof PointPF3) {
			other = (PointPF3) obj;
			result = this.getX() == other.getX() && this.getY() == other.getY();
		}
		return result;
	}

	@Override
	public int hashCode() {
		int result = 0;
		result = 41 * (41 + getX()) + getY();
		return result;
	}

}
