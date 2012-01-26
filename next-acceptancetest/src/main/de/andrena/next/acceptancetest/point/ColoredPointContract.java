package de.andrena.next.acceptancetest.point;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import de.andrena.next.Condition;

public class ColoredPointContract extends ColoredPoint {

	private ColoredPoint target = Condition.target();
	
	public ColoredPointContract(int x, int y, Color color) {
		super(x, y, color);
		if(pre()) {
			assert color != null : "color not null";
		}
		if(post()) {
			assert target.getX() == x : "x set";
			assert target.getY() == y : "y set";
			assert target.getColor() == color : "color set";
		}
	}

}