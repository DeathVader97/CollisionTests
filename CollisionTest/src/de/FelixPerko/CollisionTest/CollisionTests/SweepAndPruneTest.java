package de.FelixPerko.CollisionTest.CollisionTests;

import java.sql.Savepoint;
import java.util.ArrayList;
import de.FelixPerko.CollisionTest.DynamicDimentionalObject;
import de.FelixPerko.CollisionTest.StaticPointObject;
import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.Box;
import de.FelixPerko.CollisionTest.SweepAndPrune.EndPointOwner;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAP;

public class SweepAndPruneTest extends CollisionTest {
	
	// http://www.codercorner.com/SAP.pdf
	
	SAP sapInstance;
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
//		System.out.println("SAP init");
		sapInstance = new SAP(objects.size());
		for (TestObject o : objects){
			sapInstance.addObject(o.getEndPointOwner());
		}
		sapInstance.update();
//		System.out.println("finish init");
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
//		System.out.println("SAP tick");
		sapInstance.update();
	}

	@Override
	public void addObject(TestObject newObject) {
		sapInstance.addObject(newObject.getEndPointOwner());
	}

	@Override
	public void removeObject(TestObject removeObject) {
		if (removeObject instanceof DynamicDimentionalObject)
			((Box)removeObject.getEndPointOwner()).removed = true;
		sapInstance.removeObject(removeObject.getEndPointOwner());
	}

}
