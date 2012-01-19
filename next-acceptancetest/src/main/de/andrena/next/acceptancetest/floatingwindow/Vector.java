package de.andrena.next.acceptancetest.floatingwindow;

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
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
}
