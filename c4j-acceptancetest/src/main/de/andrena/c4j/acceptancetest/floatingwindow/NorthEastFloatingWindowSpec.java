package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.Contract;

@Contract(NorthEastFloatingWindowSpecContract.class)
public interface NorthEastFloatingWindowSpec extends Window {
	
	void move(Vector vector);
	
}