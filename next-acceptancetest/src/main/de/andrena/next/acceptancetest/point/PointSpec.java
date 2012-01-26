package de.andrena.next.acceptancetest.point;

import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.acceptancetest.object.ObjectSpec;

@Contract(PointSpecContract.class)
public interface PointSpec extends ObjectSpec {
	
	@Pure
	int getX();

	@Pure
	int getY();

	void setX(int x);

	void setY(int y);

}