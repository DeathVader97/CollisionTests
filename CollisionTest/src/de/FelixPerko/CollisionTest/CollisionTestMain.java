package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.BruteForceTest;
import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	public static int totalDynamicObjects = 5000;
	public static int totalStaticObjects = 50000;
	public static double maxSpeed = 10;
	public static double collisionDistance = 2;
	public static Vector2d bounds = new Vector2d(1000, 1000);
	
	public static long msToSek = 1000000000L;
	public static long simulationTime = 10*msToSek;
	public static long warmup = 10*msToSek;
	public static int changesPerSecond = 0;
	
	public static CollisionTest currentTest;
	
	public static ArrayList<TestObject> objects = new ArrayList<>();
	public static ArrayList<DynamicDimentionalObject> updateObjects = new ArrayList<>();
	static ArrayList<CollisionTest> tests = new ArrayList<>();
	
	private static WindowManager windowManager = new WindowManager(true);
	
	public static void main(String[] args) {
		windowManager.init();
		addTests();
		initObjects();
		for (int i = 1 ; i <= 10 ; i++){
//			TickHelper.setThreadCount(i);
//			simulationTime = (long) (i*0.2*1000000000l);
//			changesPerSecond = i*20;
			int testNr = 0;
			for (CollisionTest test : tests){
				currentTest = test;
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
//		tests.add(new SweepAndPruneGridTest(1, 1));
//		tests.add(new SweepAndPruneGridTest(5, 5));
		tests.add(new SweepAndPruneGridTest(10, 10));
//		tests.add(new SweepAndPruneGridTest(20, 20));
//		tests.add(new SweepAndPruneGridTest(40, 40));
	}

	private static void initObjects() {
//		DynamicDimentionalObject o1 = new DynamicDimentionalObject(
//			new Vector2d(bounds.x*0.5, bounds.y*0.5),
//			new Vector2d(50, 80));
//		DynamicDimentionalObject o2 = new DynamicDimentionalObject(
//				new Vector2d(bounds.x*0.5, bounds.y*0.5),
//				new Vector2d(100, 0));
//		objects.add(o1);
//		updateObjects.add(o1);
//		objects.add(o2);
//		updateObjects.add(o2);
		
//		for (int i = 0 ; i < 100 ; i++){
//			DynamicDimentionalObject o1 = new DynamicDimentionalObject(
//					new Vector2d(bounds.x*0.5, bounds.y*0.5),
//					new Vector2d(2000*(Math.random()-0.5), 2000*(Math.random()-0.5)));
//			objects.add(o1);
//			updateObjects.add(o1);
//		}
		
		objects.clear();
		updateObjects.clear();
		for (int i = 0 ; i < totalStaticObjects ; i++){
			StaticPointObject o = new StaticPointObject(
					new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()));
			objects.add(o);
		}
		for (int i = 0 ; i < totalDynamicObjects ; i++){
			DynamicDimentionalObject o = new DynamicDimentionalObject(
					new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
					new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
			objects.add(o);
			updateObjects.add(o);
		}
	}
	
	static ArrayList<Double> timings = new ArrayList<>();
	
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
			if ((currentTime-t1) > simulationTime+warmup){

//				System.out.println();
//				System.out.println();
				for (Double time : timings)
					System.out.println(time);
//				System.out.println();
//				System.out.println();
//				System.out.println(updateSAPsTime);
//				System.out.println(updateObjectTime);
				return currentTime-t1-warmup;
			}
			if (lastTime == 0){
				lastTime = currentTime;
			} else {
//				timings.add(((double)currentTime-lastTime)/1000000);
			}
			double deltaT = ((double)currentTime-lastTime)/1000000000;
			
			if (changesPerSecond != 0){
				int changes = (int)Math.round(changesPerSecond*((currentTime-lastTime)/1000000000.0));
				for (int i = 0 ; i < changes ; i++){
					TestObject removeObject = objects.get((int)(Math.random()*objects.size()));
					objects.remove(removeObject);
					updateObjects.remove(removeObject);
					test.removeObject(removeObject);
				}
				for (int i = 0 ; i < changes ; i++){
					DynamicDimentionalObject newObject = new DynamicDimentionalObject(
							new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
							new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
					objects.add(newObject);
					updateObjects.add(newObject);
					test.addObject(newObject);
				}
			}
			
			long t2 = System.nanoTime();
			TickHelper.moveObjects(deltaT);
//			System.out.println("objects moved.");
			long t3 = System.nanoTime();
			test.tick((currentTime-t1) > warmup);
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
