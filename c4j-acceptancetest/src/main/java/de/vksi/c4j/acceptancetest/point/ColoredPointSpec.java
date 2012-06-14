package de.vksi.c4j.acceptancetest.point;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;

@ContractReference(ColoredPointSpecContract.class)
public interface ColoredPointSpec extends PointSpec {
	
	@Pure
	Color getColor();

	void setColor(Color color);

}