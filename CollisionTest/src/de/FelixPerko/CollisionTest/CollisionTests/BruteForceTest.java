package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTestMain;
import de.FelixPerko.CollisionTest.TestObject;

public class BruteForceTest extends CollisionTest {

	@Override
	protected void onInit(ArrayList<TestObject> objects) {
		
	}

	@Override
	protected void onTick(ArrayList<TestObject> objects) {
		double collisionDistance = CollisionTestMain.collisionDistance;
		double collisionDistanceSq = collisionDistance*collisionDistance;
		int size = CollisionTestMain.totalObjects;
		for (int i = 0 ; i < objects.size() ; i++){
			for (int j = i+1 ; j < objects.size() ; j++){
				TestObject o1 = objects.get(i);
				TestObject o2 = objects.get(j);
				boolean collide = o1.getPos().distSq(o2.getPos()) < collisionDistanceSq;
			}
		}
	}

}
