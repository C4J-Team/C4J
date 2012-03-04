package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.Contract;

@Contract(NorthEastAndSouthWestFloatingWindowContract.class)
public class NorthEastAndSouthWestFloatingWindow implements NorthEastFloatingWindowSpec, SouthWestFloatingWindowSpec {

	private Vector upperLeftCorner;		
	private int width;		
	private int height;
	
	public NorthEastAndSouthWestFloatingWindow(Vector upperLeftCorner, int width, int height) {
		this.upperLeftCorner = upperLeftCorner;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public Vector getUpperLeftCorner() {
		return upperLeftCorner;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void move(Vector vector) {
		upperLeftCorner.add(vector);
	}
	
}
