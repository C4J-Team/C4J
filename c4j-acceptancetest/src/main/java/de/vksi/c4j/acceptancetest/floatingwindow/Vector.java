package de.vksi.c4j.acceptancetest.floatingwindow;

import de.vksi.c4j.Pure;

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
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
}
