package de.andrena.c4j.acceptancetest.point;

import de.andrena.c4j.Contract;

@Contract(PointContract.class)
public class Point implements PointSpec {

	private int x;
	private int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (this == obj) {
			result = true;
		} else if (obj == null) {
			result = false;
		} else if (getClass() != obj.getClass()) {
			result = false;
		} else {
			Point other = (Point) obj;
			result = x == other.x && y == other.y;
		}
		return result;
	}

	@Override
	public int hashCode() {
		int result = 0;
		result = 41 * (41 + getX()) + getY();
		return result;
	}

	@Override
	public String toString() {
		String result = this.getClass().getName() + " : x = " + getX();
		result = result + " : y = " + getY();
		return result;
	}
}