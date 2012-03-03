package de.andrena.c4j.acceptancetest.point;

import de.andrena.next.Contract;

@Contract(ColoredPointContract.class)
public class ColoredPoint extends Point implements ColoredPointSpec {

	private Color color;

	public ColoredPoint(int x, int y, Color color) {
		super(x, y);
		setColor(color);
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (this == obj) {
			result = true;
		} else if (!super.equals(obj)) {
			result = false;
		} else if (getClass() != obj.getClass()) {
			result = false;
		} else {
			ColoredPoint other = (ColoredPoint) obj;
			result = color == other.color;
		}
		return result;
	}

	@Override
	public String toString() {
		String result = super.toString();
		result = result + " : Color = " + color;
		return result;
	}

}