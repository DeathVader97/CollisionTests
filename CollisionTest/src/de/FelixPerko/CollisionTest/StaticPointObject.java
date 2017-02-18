package de.FelixPerko.CollisionTest;

import de.FelixPerko.CollisionTest.SweepAndPrune.EndPointOwner;

public class StaticPointObject extends TestObject{
	
	Point collisionPoint; 
	
	public StaticPointObject(Vector2d pos) {
		this.pos = pos;
		collisionPoint = new Point(pos);
	}

	@Override
	public EndPointOwner getEndPointOwner() {
		return collisionPoint;
	}
}
