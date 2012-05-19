package de.andrena.c4j.acceptancetest.point;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.Target;

public class ColoredPointContract extends ColoredPoint {

	@Target
	private ColoredPoint target;

	public ColoredPointContract(int x, int y, Color color) {
		super(x, y, color);
		if (pre()) {
			assert color != null : "color not null";
		}
		if (post()) {
			assert target.getX() == x : "x set";
			assert target.getY() == y : "y set";
			assert target.getColor() == color : "color set";
		}
	}

}