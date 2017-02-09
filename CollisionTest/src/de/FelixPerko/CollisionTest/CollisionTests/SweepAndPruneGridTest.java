package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.DynamicDimentionalObject;
import de.FelixPerko.CollisionTest.Point;
import de.FelixPerko.CollisionTest.StaticPointObject;
import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAP;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAPGrid;

public class SweepAndPruneGridTest extends CollisionTest {
	
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
