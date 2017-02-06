package de.FelixPerko.CollisionTest.CollisionTests;

import java.util.ArrayList;

import de.FelixPerko.CollisionTest.CollisionTestMain;
import de.FelixPerko.CollisionTest.TestObject;

public abstract class CollisionTest {
	
	long initTime = 0;
	long tickTime = 0;
	int ticks = 0;
	
	public void init(){
		long t1 = System.nanoTime();
		onInit(CollisionTestMain.objects);
		long t2 = System.nanoTime();
		initTime = t2-t1;
	}
	
	public void tick(){
		long t1 = System.nanoTime();
		onTick(CollisionTestMain.objects);
		long t2 = System.nanoTime();
		tickTime = t2-t1;
		ticks++;
	}
	
	public void printData(long totalTime, long expectedTime){
		double factor = expectedTime/(double)totalTime;
//		System.out.println();
//		System.out.println("Result "+getClass().getSimpleName()+":");
//		System.out.println("init: "+initTime*factor);
//		System.out.println("tick: "+(tickTime*factor/ticks));
		System.out.print((int)(ticks*factor));
		ticks = 0;
	}
	
	protected abstract void onInit(ArrayList<TestObject> objects);
	protected abstract void onTick(ArrayList<TestObject> objects);
}
