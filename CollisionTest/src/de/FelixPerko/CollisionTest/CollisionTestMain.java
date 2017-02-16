package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneTest;

public class CollisionTestMain {
	
	/*
	 * This is a test program for collision detection algorithms.
	 */
	
	private static WindowManager windowManager = new WindowManager(false);
	public static long sekToNs = 1000000000L;
	
	public static int totalDynamicObjects = 5000;
	public static int totalStaticObjects = 50000;
	public static double maxSpeed = 100;
	public static double collisionDistance = 2; //the minimal collision distance, half of the AABB width/height
	public static int changesPerSecond = 0; //insertions and deletions that will be performed every second
	
	public static Vector2d bounds = new Vector2d(1000, 1000); //size of simulation plane
	
	public static long simulationTime = 30*sekToNs;
	public static long warmup = 10*sekToNs;
	
	public static CollisionTest currentTest;
	
	public static ArrayList<TestObject> objects = new ArrayList<>(); //list of all objects
	public static ArrayList<DynamicDimentionalObject> updateObjects = new ArrayList<>(); //list of moving objects that need to be updated
	static ArrayList<CollisionTest> tests = new ArrayList<>(); //list of tests that will be performed
	
	public static void main(String[] args) {
		
		windowManager.init();
		
		addTests();
		
		initObjects();
		
		for (int i = 1 ; i <= 1 ; i++){
			int testNr = 0;
			for (CollisionTest test : tests){
				currentTest = test;
				test.init(); //prepare test
				long timeUsed = loop(test); //execute test
				
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

	static double changesValue = 0;
	
	private static long loop(CollisionTest test) {
		//init
		long lastTime = 0;
		changesValue = 0;
		long startTime = System.nanoTime();
		
		//main loop
		long currentTime = System.nanoTime();
		while ((currentTime-startTime) <= simulationTime+warmup){
			
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/sekToNs;
			
			//perform deletions, insertions
			processChanges(currentTime, lastTime, test);
			
			//move objects
			TickHelper.moveObjects(deltaT);
			
			//perform test tick
			test.tick((currentTime-startTime) > warmup);
			
			//log tick time
			long endTime = System.nanoTime();
			if ((currentTime-startTime) > warmup){
				ticktimes.add((endTime-currentTime));
			}
			
			//render if window is enabled
			windowManager.update();
			
			lastTime = currentTime;
			currentTime = System.nanoTime();
		}
		
		//time is over, finish test
		long median = 0;
		long max = 0;
		for (int i = ticktimes.size()-1 ; i >= 0 ; i--){
			long v = ticktimes.get(i);
			median += v;
			if (v > max)
				max = v;
		}
		median /= ticktimes.size();
		for (int i = ticktimes.size()-1 ; i >= 0 ; i--){
			System.out.println(ticktimes.get(i));
		}
		System.out.println("median: "+nsToMs(median,3)+"ms");
		System.out.println("max: "+nsToMs(max,3)+"ms ("+max*100/median+"%)");
		
		return currentTime-startTime-warmup;
	}
	
	private static void processChanges(long currentTime, long lastTime, CollisionTest test){
		if (changesPerSecond != 0){
			changesValue += changesPerSecond*(((double)currentTime-lastTime)/sekToNs);
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
	}
	
	private static double nsToMs(long ns, int precision){
		int precisionValue = (int) Math.pow(10, precision);
		return (double)(ns/(1000000/precisionValue))/precisionValue;
	}
}
