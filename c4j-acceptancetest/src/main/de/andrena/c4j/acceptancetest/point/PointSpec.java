package de.andrena.c4j.acceptancetest.point;

import de.andrena.c4j.acceptancetest.object.ObjectSpec;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

@ContractReference(PointSpecContract.class)
public interface PointSpec extends ObjectSpec {
	
	@Pure
	int getX();

	@Pure
	int getY();

	void setX(int x);

	void setY(int y);

}