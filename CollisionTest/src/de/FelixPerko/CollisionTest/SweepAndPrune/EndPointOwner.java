package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;

public abstract class EndPointOwner {
	static int ID_COUNTER = 0;

	public int id;
	
	public EndPointOwner(){
		id = ID_COUNTER;
		ID_COUNTER++;
	}
	
	public int[] saps;
	public SAPGrid grid;
	
}
