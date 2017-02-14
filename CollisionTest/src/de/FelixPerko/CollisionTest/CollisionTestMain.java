package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.BruteForceTest;
import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	public static int totalDynamicObjects = 10000;
	public static int totalStaticObjects = 100000;
	public static double maxSpeed = 200;
	public static double collisionDistance = 2;
	public static Vector2d bounds = new Vector2d(1000, 1000);
	
	public static long msToSek = 1000000000L;
	public static long simulationTime = 60*msToSek;
	public static long warmup = 10*msToSek;
	public static int changesPerSecond = 0;
	
	public static CollisionTest currentTest;
	
	public static ArrayList<TestObject> objects = new ArrayList<>();
	public static ArrayList<DynamicDimentionalObject> updateObjects = new ArrayList<>();
	static ArrayList<CollisionTest> tests = new ArrayList<>();
	
	private static WindowManager windowManager = new WindowManager(false);
	
	public static void main(String[] args) {
		windowManager.init();
		addTests();
		initObjects();
		for (int i = 1 ; i <= 1 ; i++){
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
	
	static ArrayList<Long> timings = new ArrayList<>();
	
	private static long loop(CollisionTest test) {
		long t1 = System.nanoTime();
		long lastTime = 0;
		
		double changesValue = 0;
		
		while (true){
			long tt1 = System.nanoTime();
			long currentTime = System.nanoTime();
			if ((currentTime-t1) > simulationTime+warmup){
				for (int i = timings.size()-1 ; i >= 0 ; i--)
					System.out.println(timings.get(i));
				return currentTime-t1-warmup;
			}
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/1000000000;
			long tt2 = System.nanoTime();
			if (changesPerSecond != 0){
				changesValue += changesPerSecond*((currentTime-lastTime)/1000000000.0);
				int changes = (int)changesValue;
				changesValue -= changes;
				for (int i = 0 ; i < changes ; i++){
					TestObject removeObject = updateObjects.get((int)(Math.random()*updateObjects.size()));
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
			long tt3 = System.nanoTime();
			TickHelper.moveObjects(deltaT);
			long tt4 = System.nanoTime();
			test.tick((currentTime-t1) > warmup);
			long tt5 = System.nanoTime();
			long endTime = System.nanoTime();
			if ((currentTime-t1) > warmup){
				timings.add((endTime-currentTime));
				double tg = tt5-tt1;
//				System.out.println((tt5-tt1)/1000000);
//				System.out.println((int)(10*(tt2-tt1)/tg)+", "+(int)(10*(tt3-tt2)/tg)+", "+(int)(10*(tt4-tt3)/tg)+", "+(int)(10*(tt5-tt4)/tg));
			}
			windowManager.update();
			lastTime = currentTime;
		}
	}
}
