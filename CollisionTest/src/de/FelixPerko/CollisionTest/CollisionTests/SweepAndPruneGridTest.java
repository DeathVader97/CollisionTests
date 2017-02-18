package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.DynamicDimentionalObject;
import de.FelixPerko.CollisionTest.Point;
import de.FelixPerko.CollisionTest.StaticPointObject;
import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.Box;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAP;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAPGrid;

public class SweepAndPruneGridTest extends CollisionTest {
	
	/*
	 * A grid of Sweep and Prune instances is used.
	 * 
	 * This spatial partition provides several advantages:
	 * - limits comparisons with objects that overlap on one axis but are actually far away (-> put in different SAPs)
	 * - faster insertions and deletions due to shorter lists
	 * 
	 * And disadvantages:
	 * - objects that intersect multiple SAPs have to be maintained in all of them
	 * - more insertions and deletions due to movement through multiple SAPs
	 * - AABBs can't be bigger than a SAP-cell in this implementation
	 * 
	 * More information about the default SAP algorithm can be found in the class CollisionTests/SweepAndPruneTest
	 */
	
	public SAPGrid grid;
	int w,h;
	
	public SweepAndPruneGridTest(int w, int h) {
		this.w = w;
		this.h = h;
	}
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
		grid = new SAPGrid(w, h, 20000, 1000, 1000);
		for (TestObject o : objects){
			o.getEndPointOwner().grid = grid;
			o.getEndPointOwner().saps = new int[]{-1,-1,-1,-1};
			if (o instanceof StaticPointObject)
				grid.updatePos((Point)((StaticPointObject) o).getEndPointOwner());
			else
				grid.updatePos(((DynamicDimentionalObject) o).getBox());
		}
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
		grid.tick();
	}

	@Override
	public void addObject(TestObject newObject) {
		newObject.getEndPointOwner().grid = grid;
		newObject.getEndPointOwner().saps = new int[]{-1,-1,-1,-1};
	}

	@Override
	public void removeObject(TestObject removeObject) {
		if (removeObject instanceof DynamicDimentionalObject)
			((Box)removeObject.getEndPointOwner()).removed = true;
		int[] removeObjectSaps = removeObject.getEndPointOwner().saps;
		for (int i = 0 ; i < removeObjectSaps.length ; i++){
			if (removeObjectSaps[i] != -1){
				grid.saps[removeObjectSaps[i]].removeObject(removeObject.getEndPointOwner());
			}
		}
	}
	
//	@Override
//	public void printData(long totalTime, long expectedTime) {
//		double factor = expectedTime/(double)totalTime;
////		System.out.println();
////		System.out.println("Result "+getClass().getSimpleName()+":");
////		System.out.println("init: "+initTime*factor);
////		System.out.println("tick: "+(tickTime*factor/ticks));
//		System.out.print(Math.round(ticks*factor*100)/100.0+" "+);
//		ticks = 0;
//	}
}
