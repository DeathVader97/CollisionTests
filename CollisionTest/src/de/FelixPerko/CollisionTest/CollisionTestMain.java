package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	public static int totalDynamicObjects = 10000;
	public static int totalStaticObjects = 100000;
	public static double maxSpeed = 100;
	
	public static double collisionDistance = 2;
	public static Vector2d bounds = new Vector2d(1000, 1000);
	
	public static long msToSek = 1000000000L;
	public static long simulationTime = 30*msToSek;
	public static long warmup = 10*msToSek;
	public static int changesPerSecond = 0;
	
	public static CollisionTest currentTest;
	
	public static ArrayList<TestObject> objects = new ArrayList<>();
	public static ArrayList<DynamicDimentionalObject> updateObjects = new ArrayList<>();
	static ArrayList<CollisionTest> tests = new ArrayList<>();
	
	private static WindowManager windowManager = new WindowManager(false);
	
	public static void main(String[] args) {
		
//		DynamicDimentionalObject o = new DynamicDimentionalObject(
//				new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
//				new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
//		DynamicDimentionalObject o2 = new DynamicDimentionalObject(
//				new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
//				new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
//		SweepAndPruneTest test = new SweepAndPruneTest();
//		objects.add(o);
//		updateObjects.add(o);
//		test.init();
//		objects.add(o2);
//		updateObjects.add(o2);
//		test.addObject(o2);
//		test.tick(false);
		
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
					System.out.print("; ");
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
		//delete current objects
		objects.clear();
		updateObjects.clear();
		
		//add random static objects
		for (int i = 0 ; i < totalStaticObjects ; i++){
			StaticPointObject o = new StaticPointObject(
					new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()));
			objects.add(o);
		}
		
		//add random dynamic objects
		for (int i = 0 ; i < totalDynamicObjects ; i++){
			DynamicDimentionalObject o = new DynamicDimentionalObject(
					new Vector2d(bounds.x*Math.random(), bounds.y*Math.random()),
					new Vector2d(maxSpeed*2*(Math.random()-0.5), maxSpeed*2*(Math.random()-0.5)));
			objects.add(o);
			updateObjects.add(o);
		}
	}
	
	static ArrayList<Long> ticktimes = new ArrayList<>();
	
	private static long loop(CollisionTest test) {
		long t1 = System.nanoTime();
		long lastTime = 0;
		
		double changesValue = 0;
		
		while (true){
			long tt1 = System.nanoTime();
			long currentTime = System.nanoTime();
			
			//stop if time is over
			if ((currentTime-t1) > simulationTime+warmup){
				
				double time = 0;
				for (int i = ticktimes.size()-1 ; i >= 0 ; i--){
//					time += (double)ticktimes.get(i)/msToSek;
//					System.out.println((""+((int)(time*100)/100.0)).replace('.', ',')+";"+ticktimes.get(i));
					System.out.println(ticktimes.get(i));
				}
				
				return currentTime-t1-warmup;
			}
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/msToSek;
			long tt2 = System.nanoTime();
			
			//perform deletions, insertions
			if (changesPerSecond != 0){
				changesValue += changesPerSecond*(((double)currentTime-lastTime)/msToSek);
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
			
			//move objects
			TickHelper.moveObjects(deltaT);
			long tt4 = System.nanoTime();
			
			//perform test tick
			test.tick((currentTime-t1) > warmup);
			long tt5 = System.nanoTime();
			
			//log tick time
			long endTime = System.nanoTime();
			if ((currentTime-t1) > warmup){
//				ticktimes.add((endTime-currentTime));
//				double tg = tt5-tt1;
//				System.out.println((tt5-tt1)/1000000);
//				System.out.println((int)(10*(tt2-tt1)/tg)+", "+(int)(10*(tt3-tt2)/tg)+", "+(int)(10*(tt4-tt3)/tg)+", "+(int)(10*(tt5-tt4)/tg));
			}
			
			//render if window is enabled
			windowManager.update();
			
			lastTime = currentTime;
		}
	}
}
