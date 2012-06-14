package de.vksi.c4j.acceptancetest.point.pitfalls;

import de.vksi.c4j.acceptancetest.point.Color;

public class ColoredPointPF4 extends PointPF4 {

	private final Color color;

	public ColoredPointPF4(int x, int y, Color color) {
		super(x, y);
		this.color = color;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		ColoredPointPF4 other = null;
		if (obj instanceof ColoredPointPF4) {
			other = (ColoredPointPF4) obj;
			result = super.equals(other) && this.color.equals(other.color);
		}
		return result;
	}

}
