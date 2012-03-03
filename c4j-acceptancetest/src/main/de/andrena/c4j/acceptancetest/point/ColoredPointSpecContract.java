package de.andrena.c4j.acceptancetest.point;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static de.andrena.next.Condition.result;
import de.andrena.next.Target;

public class ColoredPointSpecContract implements ColoredPointSpec {

	@Target
	private ColoredPointSpec target;

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
		// No contracts identified yet
	}

	@Override
	public void setY(int y) {
		// No contracts identified yet
	}

	@Override
	public Color getColor() {
		if (post()) {
			Color result = result(Color.class);
			assert result != null : "result not null";
		}
		return ignored();
	}

	@Override
	public void setColor(Color color) {
		if (pre()) {
			assert color != null : "color not null";
		}
		if (post()) {
			assert target.getColor() == color : "color set";
		}
	}

}