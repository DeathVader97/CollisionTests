package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	public static int totalObjects = 1000;
	public static double maxSpeed = 100;
	public static double collisionDistance = 10;
	public static Vector2d bounds = new Vector2d(1000, 1000);
	public static long simulationTime = (long)(60*1000000000l);
	
	public static ArrayList<TestObject> objects = new ArrayList<>();
	static ArrayList<CollisionTest> tests = new ArrayList<>();
	
	private static WindowManager windowManager = new WindowManager();
	
	public static void main(String[] args) {
		windowManager.init();
		addTests();
		for (int i = 1 ; i <= 1 ; i++){
			totalObjects = 10000*i;
			initObjects();
	//		System.out.println("testing for "+totalObjects+" objects");
//			System.out.print(totalObjects+",");
			for (CollisionTest test : tests){
				test.init();
				long timeUsed = loop(test);
	//			System.out.println("time Used: "+(timeUsed/1000000000.0));
				test.printData(timeUsed, 1000000000l);
				System.out.print(",");
			}
			System.out.println();
		}
	}

	private static void addTests() {
//		tests.add(new BruteForceTest());
		tests.add(new SweepAndPruneTest());
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
		while (true){
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			long currentTime = System.nanoTime();
			if ((currentTime-t1) > simulationTime){
				return currentTime-t1;
			}
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/simulationTime;
			TickHelper.moveObjects(deltaT);
			test.tick();
			windowManager.update();
			lastTime = currentTime;
		}
	}
}
