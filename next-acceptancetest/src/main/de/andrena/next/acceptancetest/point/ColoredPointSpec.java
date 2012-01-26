package de.andrena.next.acceptancetest.point;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(ColoredPointSpecContract.class)
public interface ColoredPointSpec extends PointSpec {
	
	@Pure
	Color getColor();

	void setColor(Color color);

}