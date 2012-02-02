package de.andrena.next.acceptancetest.point;

import static de.andrena.next.Condition.post;
import de.andrena.next.Target;

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