package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.Contract;

@Contract(SouthWestFloatingWindowSpecContract.class)
public interface SouthWestFloatingWindowSpec extends Window {

	void move(Vector vector);
	
}