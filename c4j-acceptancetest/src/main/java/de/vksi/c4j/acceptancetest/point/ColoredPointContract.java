package de.vksi.c4j.acceptancetest.point;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.Target;

public class ColoredPointContract extends ColoredPoint {

	@Target
	private ColoredPoint target;

	public ColoredPointContract() {
		super(0, 0, null);
	}

	@ConstructorContract
	public void constructor(int x, int y, Color color) {
		if (preCondition()) {
			assert color != null : "color not null";
		}
		if (postCondition()) {
			assert target.getX() == x : "x set";
			assert target.getY() == y : "y set";
			assert target.getColor() == color : "color set";
		}
	}

}