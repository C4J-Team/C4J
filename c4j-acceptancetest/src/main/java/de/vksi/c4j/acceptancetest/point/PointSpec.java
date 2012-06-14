package de.vksi.c4j.acceptancetest.point;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.object.ObjectSpec;

@ContractReference(PointSpecContract.class)
public interface PointSpec extends ObjectSpec {
	
	@Pure
	int getX();

	@Pure
	int getY();

	void setX(int x);

	void setY(int y);

}