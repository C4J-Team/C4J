package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.Pure;

public interface Window {
	
	Vector getUpperLeftCorner();
	
	@Pure
	int getWidth();
	
	@Pure
	int getHeight();
	
}