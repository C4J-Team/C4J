package de.andrena.c4j.acceptancetest.point;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(ColoredPointSpecContract.class)
public interface ColoredPointSpec extends PointSpec {
	
	@Pure
	Color getColor();

	void setColor(Color color);

}