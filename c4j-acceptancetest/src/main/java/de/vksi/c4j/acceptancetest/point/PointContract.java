package de.vksi.c4j.acceptancetest.point;

import static de.vksi.c4j.Condition.postCondition;
import de.vksi.c4j.Target;

public class PointContract extends Point {

	@Target
	private Point target;

	public PointContract(int x, int y) {
		super(x, y);
		if (postCondition()) {
			assert target.getX() == x : "x set";
			assert target.getY() == y : "y set";
		}
	}

}