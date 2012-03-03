package de.andrena.c4j.acceptancetest.point;

import de.andrena.c4j.acceptancetest.object.ObjectSpec;
import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(PointSpecContract.class)
public interface PointSpec extends ObjectSpec {
	
	@Pure
	int getX();

	@Pure
	int getY();

	void setX(int x);

	void setY(int y);

}