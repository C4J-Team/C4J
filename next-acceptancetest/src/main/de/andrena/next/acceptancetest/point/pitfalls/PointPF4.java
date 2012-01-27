package de.andrena.next.acceptancetest.point.pitfalls;

import de.andrena.next.acceptancetest.object.ObjectSpec;

public class PointPF4 implements ObjectSpec {

	private int x;
	private int y;
	
	private boolean hashCodeSet;
	private final int hashCode;

	public PointPF4(int x, int y) {
		this.x = x;
		this.y = y;
		hashCode = hashCode();
	}

	public int getX() {
		return x;
	}

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
		PointPF4 other = null;
		if (obj instanceof PointPF4) {
			other = (PointPF4) obj;
			result = this.getX() == other.getX() && this.getY() == other.getY();
		}
		return result;
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (!hashCodeSet) {
			result = 41 * (41 + getX()) + getY();
			hashCodeSet = true;
		}
		return result;
	}

}
