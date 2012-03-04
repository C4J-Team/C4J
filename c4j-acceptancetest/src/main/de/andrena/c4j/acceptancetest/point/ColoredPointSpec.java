package de.andrena.c4j.acceptancetest.point;

import de.andrena.c4j.Contract;
import de.andrena.c4j.Pure;

@Contract(ColoredPointSpecContract.class)
public interface ColoredPointSpec extends PointSpec {
	
	@Pure
	Color getColor();

	void setColor(Color color);

}