package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTestMain;
import de.FelixPerko.CollisionTest.DynamicDimentionalObject;
import de.FelixPerko.CollisionTest.TestObject;

public class BruteForceTest extends CollisionTest {
	
	/*
	 * Checks the distance
	 * 		from every moving object
	 * 		to every other object
	 * 		every frame.
	 * Isn't actually functional, just a dummy to get an idea of the performance achieved using trivial brute force
	 */
	
	@Override
	protected void onInit(ArrayList<TestObject> objects) {
		
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
		double collisionDistance = CollisionTestMain.collisionDistance;
		double collisionDistanceSq = collisionDistance*collisionDistance;
		for (int i = 0 ; i < CollisionTestMain.updateObjects.size() ; i++){
			for (int j = 0 ; j < objects.size() ; j++){
				TestObject o1 = CollisionTestMain.updateObjects.get(i);
				TestObject o2 = objects.get(j);
				boolean collide = o1.getPos().distSq(o2.getPos()) < collisionDistanceSq;
			}
		}
	}

	@Override
	public void addObject(TestObject newObject) {
		
	}

	@Override
	public void removeObject(TestObject removeObject) {
		
	}

}
