package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAPGrid;

public class SweepAndPruneGridTest extends CollisionTest {
	
	SAPGrid grid;
	int w,h;
	
	public SweepAndPruneGridTest(int w, int h) {
		this.w = w;
		this.h = h;
	}
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
		grid = new SAPGrid(w, h, 20000, 1000, 1000);
		for (TestObject o : objects){
			o.getBox().grid = grid;
			o.getBox().saps = new int[]{-1,-1,-1,-1};
			o.getBox().sapsValidity = new boolean[]{false,false,false,false};
			grid.updatePos(o.getBox());
		}
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
		grid.tick();
	}

	@Override
	public void addObject(TestObject newObject) {
		newObject.getBox().grid = grid;
		newObject.getBox().saps = new int[]{-1,-1,-1,-1};
		newObject.getBox().sapsValidity = new boolean[]{false,false,false,false};
	}

	@Override
	public void removeObject(TestObject removeObject) {
		for (int i = 0 ; i < 4 ; i++){
			if (removeObject.getBox().sapsValidity[i]){
				grid.saps[removeObject.getBox().saps[i]].removeObject(removeObject.getBox());
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
