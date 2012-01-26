package de.andrena.next.acceptancetest.floatingwindow;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(NorthEastFloatingWindowSpecContract.class)
public interface NorthEastFloatingWindowSpec extends Window {
	
	@Pure
	void move(Vector vector);
	
}