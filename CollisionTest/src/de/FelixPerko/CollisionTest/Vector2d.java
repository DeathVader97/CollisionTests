package de.FelixPerko.CollisionTest;

public class Vector2d {
	
	double x,y;

	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double distSq(Vector2d other) {
		double dx = x-other.x;
		double dy = y-other.y;
		return dx*dx + dy*dy;
	}
	
	public void multed(Vector2d other){
		this.x = x*other.x;
		this.y = y*other.y;
	}
	
}
