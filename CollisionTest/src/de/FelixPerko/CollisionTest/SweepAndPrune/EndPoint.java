package de.FelixPerko.CollisionTest.SweepAndPrune;

public class EndPoint {
	EndPointOwner owner;
	public float value;
	byte status; //0 = max ; 1 = min ; 2 = single point
	
	public EndPoint(EndPointOwner owner, float value, byte status) {
		this.owner = owner;
		this.value = value;
		this.status = status;
	}
	
	public float getValue(){
		return value;
	}
}
