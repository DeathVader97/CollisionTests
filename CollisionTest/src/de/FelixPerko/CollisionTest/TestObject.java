package de.FelixPerko.CollisionTest;

import de.FelixPerko.CollisionTest.SweepAndPrune.Box;

public class TestObject {
	Vector2d pos;
	Vector2d vel;
	
	Box SAPbox;
	
	public TestObject(Vector2d pos, Vector2d vel) {
		SAPbox = new Box();
		setPos(pos);
		setVel(vel);
	}
	
	public Vector2d getPos() {
		return pos;
	}
	
	public void setPos(Vector2d pos) {
		this.pos = pos;
		SAPbox.update(pos.x, pos.y, CollisionTestMain.collisionDistance*2);
	}
	
	public Vector2d getVel() {
		return vel;
	}
	
	public void setVel(Vector2d vel) {
		this.vel = vel;
	}

	public Box getBox() {
		return SAPbox;
	}
}
