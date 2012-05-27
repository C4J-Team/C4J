package de.andrena.c4j.acceptancetest.floatingwindow;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.pre;

public final class NorthEastFloatingWindowSpecContract implements NorthEastFloatingWindowSpec {

	@Override
	public Vector getUpperLeftCorner() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public int getWidth() {
		// No contracts identified yet
		return (Integer) ignored();
	}

	@Override
	public int getHeight() {
		// No contracts identified yet
		return (Integer) ignored();
	}

	@Override
	public void move(Vector vector) {
		if (pre()) {
			assert vector.getX() > 0 : "vector.x > 0";
			assert vector.getY() > 0 : "vector.y > 0";
		}
	}

}
