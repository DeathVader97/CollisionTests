package de.FelixPerko.CollisionTest.SweepAndPrune;

public class EndPoint {
	Box owner;
	float value;
	boolean isMin;
	
	public EndPoint(Box owner, float value, boolean isMin) {
		this.owner = owner;
		this.value = value;
		this.isMin = isMin;
	}
	
}
