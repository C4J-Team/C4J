package de.andrena.next.acceptancetest.floatingwindow;

import de.andrena.next.Pure;

public class Vector {
	
	private int x, y;
	
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void add(Vector vector) {
		this.x += vector.x;
		this.y += vector.y;
	}
	
	@Pure
	public int getX() {
		return x;
	}
	
	@Pure
	public int getY() {
		return y;
	}
	
}
