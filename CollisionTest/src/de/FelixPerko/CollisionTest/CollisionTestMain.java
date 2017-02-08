package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.BruteForceTest;
import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	public static int totalObjects = 100000;
	public static double maxSpeed = 500;
	public static double collisionDistance = 2;
	public static Vector2d bounds = new Vector2d(1000, 1000);
	public static long simulationTime = (long)(10*1000000000l);
	public static int changesPerSecond = 50;
	
	public static ArrayList<TestObject> objects = new ArrayList<>();
	static ArrayList<CollisionTest> tests = new ArrayList<>();
	
	private static WindowManager windowManager = new WindowManager();
	
	public static void main(String[] args) {
		windowManager.init();
		addTests();
		for (int i = 1 ; i <= 1 ; i++){
			totalObjects = i*10000;
			initObjects();
//			changesPerSecond = i*20;
			int testNr = 0;
			for (CollisionTest test : tests){
				test.init();
				long timeUsed = loop(test);
				test.printData(timeUsed, 1000000000l);
				
				if (testNr < tests.size()-1)
					System.out.print(";");
				testNr++;
			}
			System.out.println();
		}
		System.exit(0);
	}

	private static void addTests() {
//		tests.add(new BruteForceTest());
//		tests.add(new SweepAndPruneTest());
//		tests.add(new SweepAndPruneGridTest(2, 2));
//		tests.add(new SweepAndPruneGridTest(5, 5));
		tests.add(new SweepAndPruneGridTest(10, 10));
//		tests.add(new SweepAndPruneGridTest(20, 20));
//		tests.add(new SweepAndPruneGridTest(40, 40));
	}

	private static void initObjects() {
		objects.clear();
		for (int i = 0 ; i < totalObjects ; i++){
			objects.add(new TestObject(
					new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
					new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5))));
		}
	}

	private static long loop(CollisionTest test) {
		long t1 = System.nanoTime();
		long lastTime = 0;
		
		long updateObjectTime = 0;
		long updateSAPsTime = 0;
		while (true){
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			long currentTime = System.nanoTime();
			if ((currentTime-t1) > simulationTime){

				System.out.println();
				System.out.println(updateSAPsTime);
				System.out.println(updateObjectTime);
				return currentTime-t1;
			}
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/simulationTime;
			
			if (changesPerSecond != 0){
				int changes = (int)Math.round(changesPerSecond*((currentTime-lastTime)/1000000000.0));
				for (int i = 0 ; i < changes ; i++){
					TestObject removeObject = objects.get((int)(Math.random()*objects.size()));
					objects.remove(removeObject);
					test.removeObject(removeObject);
				}
				for (int i = 0 ; i < changes ; i++){
					TestObject newObject = new TestObject(
							new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
							new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
					objects.add(newObject);
					test.addObject(newObject);
				}
			}
			
			long t2 = System.nanoTime();
			TickHelper.moveObjects(deltaT);
//			System.out.println("objects moved.");
			long t3 = System.nanoTime();
			test.tick();
//			System.out.println("collision tested.");
			long t4 = System.nanoTime();
			updateObjectTime += t3-t2;
			updateSAPsTime += t4-t3;
			windowManager.update();
			lastTime = currentTime;
//			System.out.println(System.nanoTime()-currentTime);
		}
	}
}
