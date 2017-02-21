package de.FelixPerko.CollisionTest;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTests.BruteForceTest;
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
	
	static long simulationTime = 30*sekToNs;
	static long warmup = 5*sekToNs;
	
	public static CollisionTest currentTest;
	
	public static ArrayList<TestObject> objects = new ArrayList<>(); //list of all objects
	public static ArrayList<DynamicDimentionalObject> updateObjects = new ArrayList<>(); //list of moving objects that need to be updated
	static ArrayList<CollisionTest> tests = new ArrayList<>(); //list of tests that will be performed
	
	public static void main(String[] args) {
		
		windowManager.init();
		
		addTests();
		
		for (int i = 0 ; i <= 4 ; i++){
			switch (i){
			case 0: totalStaticObjects = totalDynamicObjects/2; break;
			case 1: totalStaticObjects = totalDynamicObjects; break;
			case 2: totalStaticObjects = totalDynamicObjects*2; break;
			case 3: totalStaticObjects = totalDynamicObjects*5; break;
			case 4: totalStaticObjects = totalDynamicObjects*10; break;
			}
			
			initObjects();
			
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
//			System.out.println((currentTime-startTime)+" "+(simulationTime+warmup)+" "+true);
			
			if (lastTime == 0){
				lastTime = currentTime;
			}
			double deltaT = ((double)currentTime-lastTime)/sekToNs;
			
			//perform deletions, insertions
			processChanges(deltaT, test);
			
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
//		long average = 0;
//		long max = 0;
//		for (int i = ticktimes.size()-1 ; i >= 0 ; i--){
//			long v = ticktimes.get(i);
//			average += v;
//			if (v > max)
//				max = v;
//		}
//		average /= ticktimes.size();
//		for (int i = ticktimes.size()-1 ; i >= 0 ; i -= 100){
//			long sum = ticktimes.get(i);
//			for (int j = i ; j > i-100 && j >= 0 ; j--)
//				sum += ticktimes.get(j);
//			System.out.println(sum/100);
//		}
//		System.out.println("average: "+nsToMs(average,3)+"ms");
//		System.out.println("max: "+nsToMs(max,3)+"ms ("+max*100/average+"%)");
		
		return currentTime-startTime-warmup;
	}
	
	private static void processChanges(double deltaT, CollisionTest test){
		if (changesPerSecond != 0){
			changesValue += changesPerSecond*deltaT;
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
