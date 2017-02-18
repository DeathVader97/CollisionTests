package de.FelixPerko.CollisionTest;

import de.FelixPerko.CollisionTest.SweepAndPrune.EndPointOwner;

public abstract class TestObject {
	Vector2d pos;

	public Vector2d getPos() {
		return pos;
	}

	public abstract EndPointOwner getEndPointOwner();
}
