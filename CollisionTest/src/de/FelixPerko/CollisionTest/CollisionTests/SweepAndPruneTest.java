package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;
import de.FelixPerko.CollisionTest.DynamicDimentionalObject;
import de.FelixPerko.CollisionTest.TestObject;
import de.FelixPerko.CollisionTest.SweepAndPrune.Box;
import de.FelixPerko.CollisionTest.SweepAndPrune.SAP;

public class SweepAndPruneTest extends CollisionTest {
	
	/*	
	 *	AABB Collision detection algorithm that sorts a list of EndPoints for every axis every tick.
	 *	If EndPoints swap positions the overlap is updated.
	 *	This implementation includes the collision with points (that don't have dimensions).
	 *	
	 *	Loosely based on the following tutorial:
	 *  http://www.codercorner.com/SAP.pdf
	 */
	
	SAP sapInstance;
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
		sapInstance = new SAP(objects.size());
		for (TestObject o : objects){
			sapInstance.addObject(o.getEndPointOwner());
		}
		sapInstance.update();
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
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
