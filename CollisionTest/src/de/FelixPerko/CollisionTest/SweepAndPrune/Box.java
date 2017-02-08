package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import de.FelixPerko.CollisionTest.TestObject;

public class Box {
	private static int ID_COUNTER = 0;
	public SAPGrid grid;
	
	int id;
	EndPoint xMin,xMax,yMin,yMax;
	public ConcurrentHashMap<Integer, Box> collisions = new ConcurrentHashMap<>();
	public TestObject object;

	public int[] saps;
	public boolean[] sapsValidity;
	
	public Box(TestObject object, SAPGrid grid) {
		id = ID_COUNTER;
		ID_COUNTER++;
		this.object = object;
		this.grid = grid;
		
		xMin = new EndPoint(this,0,true);
		xMax = new EndPoint(this,0,false);
		yMin = new EndPoint(this,0,true);
		yMax = new EndPoint(this,0,false);
	}

	public void update(double x, double y, double boxSizeHalfed) {
		xMin.value = (float)(x-boxSizeHalfed);
		xMax.value = (float)(x+boxSizeHalfed);
		yMin.value = (float)(y-boxSizeHalfed);
		yMax.value = (float)(y+boxSizeHalfed);
		if (grid != null)
			grid.updatePos(this);
	}
}
