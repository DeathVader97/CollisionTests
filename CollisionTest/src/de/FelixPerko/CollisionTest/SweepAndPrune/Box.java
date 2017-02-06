package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.HashMap;

import de.FelixPerko.CollisionTest.TestObject;

public class Box {
	private static int ID_COUNTER = 0;
	
	int id;
	EndPoint xMin,xMax,yMin,yMax;
	public HashMap<Integer, Boolean> collisions = new HashMap<>();
	public TestObject object;
	
	public Box(TestObject object) {
		id = ID_COUNTER;
		ID_COUNTER++;
		this.object = object;
		
		xMin = new EndPoint(this,0,true);
		xMax = new EndPoint(this,0,false);
		yMin = new EndPoint(this,0,true);
		yMax = new EndPoint(this,0,false);
	}

	public void update(double x, double y, double boxSize) {
		xMin.value = (float)(x-boxSize/2);
		xMax.value = (float)(x+boxSize/2);
		yMin.value = (float)(y-boxSize/2);
		yMax.value = (float)(y+boxSize/2);
	}
}
