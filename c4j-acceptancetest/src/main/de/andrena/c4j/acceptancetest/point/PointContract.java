package de.andrena.c4j.acceptancetest.point;

import static de.andrena.c4j.Condition.post;
import de.andrena.c4j.Target;

public class PointContract extends Point {

	@Target
	private Point target;

	public PointContract(int x, int y) {
		super(x, y);
		if (post()) {
			assert target.getX() == x : "x set";
			assert target.getY() == y : "y set";
		}
	}

}