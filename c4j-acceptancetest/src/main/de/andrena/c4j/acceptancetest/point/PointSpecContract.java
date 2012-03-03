package de.andrena.c4j.acceptancetest.point;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import de.andrena.next.Target;

public class PointSpecContract implements PointSpec {

	@Target
	private PointSpec target;

	@Override
	public int getX() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public int getY() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public void setX(int x) {
		if (post()) {
			assert target.getX() == x : "x set";
		}
	}

	@Override
	public void setY(int y) {
		if (post()) {
			assert target.getY() == y : "y set";
		}
	}

}