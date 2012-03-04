package de.andrena.c4j.acceptancetest.floatingwindow;

import static de.andrena.c4j.Condition.pre;

public class NorthEastAndSouthWestFloatingWindowContract extends NorthEastAndSouthWestFloatingWindow {

	public NorthEastAndSouthWestFloatingWindowContract(Vector upperLeftCorner, int width, int height) {
		super(upperLeftCorner, width, height);	
	}
	
	@SuppressWarnings("unused")
	@Override
	public void move(Vector vector) {
		if(pre()) {
			assert true : "true";
		}
	}

}
