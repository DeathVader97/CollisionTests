package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.SweepAndPrune.EndPoint;
import de.FelixPerko.CollisionTest.SweepAndPrune.EndPointOwner;

public class Point extends EndPointOwner{
	public EndPoint x,y;
	
	public Point(Vector2d pos){
		x = new EndPoint(this, (float) pos.x, (byte)2);
		y = new EndPoint(this, (float) pos.y, (byte)2);
	}
	
	public void setPos(Vector2d pos){
		x.value = (float) pos.x;
		y.value = (float) pos.y;
	}
	
	public Vector2d getPos(){
		return new Vector2d(x.value,y.value);
	}
}
