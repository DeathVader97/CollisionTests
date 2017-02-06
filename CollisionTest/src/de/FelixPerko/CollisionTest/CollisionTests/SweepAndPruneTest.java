package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;
import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAP;

public class SweepAndPruneTest extends CollisionTest {
	
	// http://www.codercorner.com/SAP.pdf
	
	SAP sapInstance;
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
//		System.out.println("SAP init");
		sapInstance = new SAP(objects.size());
		for (TestObject o : objects){
			sapInstance.addObject(o.getBox());
		}
		sapInstance.update();
//		System.out.println("finish init");
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
//		System.out.println("SAP tick");
		sapInstance.update();
	}

}
