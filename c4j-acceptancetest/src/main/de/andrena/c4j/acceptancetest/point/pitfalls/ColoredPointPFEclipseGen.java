package de.andrena.c4j.acceptancetest.point.pitfalls;

import de.andrena.c4j.acceptancetest.point.Color;

public class ColoredPointPFEclipseGen extends PointPFEclipseGen {

	private final Color color;

	public ColoredPointPFEclipseGen(int x, int y, Color color) {
		super(x, y);
		this.color = color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColoredPointPFEclipseGen other = (ColoredPointPFEclipseGen) obj;
		if (color != other.color)
			return false;
		return true;
	}

}