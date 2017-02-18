package de.FelixPerko.CollisionTest;

import de.FelixPerko.CollisionTest.SweepAndPrune.Box;
import de.FelixPerko.CollisionTest.SweepAndPrune.EndPointOwner;

public class DynamicDimentionalObject extends TestObject{
	Vector2d vel;
	
	public Box sapbox;
	
	public DynamicDimentionalObject(Vector2d pos, Vector2d vel) {
		sapbox = new Box(this, null);
		setPos(pos);
		setVel(vel);
	}
	
	public Vector2d getPos() {
		return pos;
	}
	
	public void setPos(Vector2d pos) {
		this.pos = pos;
		sapbox.update(pos.x, pos.y, CollisionTestMain.collisionDistance);
	}
	
	public Vector2d getVel() {
		return vel;
	}
	
	public void setVel(Vector2d vel) {
		this.vel = vel;
	}

	public Box getBox() {
		return sapbox;
	}

	@Override
	public EndPointOwner getEndPointOwner() {
		return sapbox;
	}

	public void positionUpdated() {
		sapbox.update(pos.x, pos.y, CollisionTestMain.collisionDistance);
	}
}
