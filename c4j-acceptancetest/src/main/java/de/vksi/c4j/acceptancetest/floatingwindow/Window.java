package de.vksi.c4j.acceptancetest.floatingwindow;

import de.vksi.c4j.Pure;

public interface Window {
	
	Vector getUpperLeftCorner();
	
	@Pure
	int getWidth();
	
	@Pure
	int getHeight();
	
}