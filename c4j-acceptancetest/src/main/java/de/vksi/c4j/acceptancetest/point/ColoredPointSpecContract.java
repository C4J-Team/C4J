package de.vksi.c4j.acceptancetest.point;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;
import static de.vksi.c4j.Condition.result;
import de.vksi.c4j.Target;

public class ColoredPointSpecContract implements ColoredPointSpec {

	@Target
	private ColoredPointSpec target;

	@Override
	public int getX() {
		// No contracts identified yet
		return (Integer) ignored();
	}

	@Override
	public int getY() {
		// No contracts identified yet
		return (Integer) ignored();
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