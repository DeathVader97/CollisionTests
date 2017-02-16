package de.FelixPerko.CollisionTest.SweepAndPrune;

public abstract class EndPointOwner {
	static int ID_COUNTER = 0;

	public Integer id;
	
	public EndPointOwner(){
		id = ID_COUNTER;
		ID_COUNTER++;
	}
	
	public int[] saps;
	public SAPGrid grid;
	
}
