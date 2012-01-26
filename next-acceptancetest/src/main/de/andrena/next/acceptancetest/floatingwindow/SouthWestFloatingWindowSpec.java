package de.andrena.next.acceptancetest.floatingwindow;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(SouthWestFloatingWindowSpecContract.class)
public interface SouthWestFloatingWindowSpec extends Window {
	
	@Pure
	void move(Vector vector);
	
}
