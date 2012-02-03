package de.andrena.next.acceptancetest.floatingwindow;

import de.andrena.next.Contract;

@Contract(SouthWestFloatingWindowSpecContract.class)
public interface SouthWestFloatingWindowSpec extends Window {

	void move(Vector vector);
	
}
