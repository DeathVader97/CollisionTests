package de.FelixPerko.CollisionTest.SweepAndPrune;

public class Box {
	private static int ID_COUNTER = 0;
	
	int id;
	EndPoint xMin,xMax,yMin,yMax;
	
	public Box() {
		id = ID_COUNTER;
		ID_COUNTER++;
		
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
