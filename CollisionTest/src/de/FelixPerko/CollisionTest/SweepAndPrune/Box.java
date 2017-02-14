package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.HashMap;
import de.FelixPerko.CollisionTest.DynamicDimentionalObject;

public class Box extends EndPointOwner{
	
	EndPoint xMin,xMax,yMin,yMax;
	public HashMap<Integer, EndPointOwner> collisions = new HashMap<>();
	public DynamicDimentionalObject object;
	public boolean removed = false; //marked for removal
	
	public Box(DynamicDimentionalObject object, SAPGrid grid) {
		this.object = object;
		this.grid = grid;
		
		xMin = new EndPoint(this,0,(byte)1);
		xMax = new EndPoint(this,0,(byte)0);
		yMin = new EndPoint(this,0,(byte)1);
		yMax = new EndPoint(this,0,(byte)0);
	}

	public void update(double x, double y, double boxSizeHalfed) {
		xMin.value = (float)(x-boxSizeHalfed);
		xMax.value = (float)(x+boxSizeHalfed);
		yMin.value = (float)(y-boxSizeHalfed);
		yMax.value = (float)(y+boxSizeHalfed);
		if (grid != null)
			grid.updatePos(this);
	}

	@Override
	public ArrayList<EndPoint> getEndPointsX() {
		ArrayList<EndPoint> list = new ArrayList<>();
		list.add(xMin);
		list.add(xMax);
		return list;
	}

	@Override
	public ArrayList<EndPoint> getEndPointsY() {
		ArrayList<EndPoint> list = new ArrayList<>();
		list.add(yMin);
		list.add(yMax);
		return list;
	}
}
