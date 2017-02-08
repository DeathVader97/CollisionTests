package de.FelixPerko.CollisionTest;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TickHelper {
	
	public static int helperThreadCount = 4;
//	public static int helperThreadCount = 1;
	public static ExecutorService es = Executors.newFixedThreadPool(helperThreadCount);
	
	public static void moveObjects(double timeFactor){
		ArrayList<TestObject> objects = CollisionTestMain.objects;
		CountDownLatch latch = new CountDownLatch(helperThreadCount);
		int s = objects.size();
		int chunkSize = s/helperThreadCount;
		for (int i = 0 ; i < helperThreadCount-1 ; i++)
			es.execute(new HelperRunnable(objects, i*chunkSize, (i+1)*chunkSize, timeFactor, latch));
		es.execute(new HelperRunnable(objects, (helperThreadCount-1)*chunkSize, s, timeFactor, latch));
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		for (TestObject o : objects){
//			double x = o.getPos().x;
//			double y = o.getPos().y;
//			x += o.getVel().x*timeFactor;
//			y += o.getVel().y*timeFactor;
//			
//			if (x < 0){
//				x = 0;
//				o.setVel(new Vector2d(-o.getVel().x, o.getVel().y));
//			} else if (x > xb){
//				x = xb;
//				o.setVel(new Vector2d(-o.getVel().x, o.getVel().y));
//			}
//			if (y < 0){
//				y = 0;
//				o.setVel(new Vector2d(o.getVel().x, -o.getVel().y));
//			} else if (y > yb){
//				y = yb;
//				o.setVel(new Vector2d(o.getVel().x, -o.getVel().y));
//			}
//			
//			o.setPos(new Vector2d(x, y));
//		}
	}
}

class HelperRunnable implements Runnable{
	
	ArrayList<TestObject> objects;
	int l,h;
	double timeFactor;
	static double xb = CollisionTestMain.bounds.x;
	static double yb = CollisionTestMain.bounds.y;
	CountDownLatch latch;
	
	public HelperRunnable(ArrayList<TestObject> objects, int l, int h, double timeFactor, CountDownLatch latch) {
		this.objects = objects;
		this.l = l;
		this.h = h;
		this.timeFactor = timeFactor;
		this.latch = latch;
	}

	@Override
	public void run() {
//		System.out.println("start update task on thread "+Thread.currentThread().getName());
		for (int i = l ; i < h ; i++){
			TestObject o = objects.get(i);
			double x = o.getPos().x;
			double y = o.getPos().y;
			x += o.getVel().x*timeFactor;
			y += o.getVel().y*timeFactor;
			
			if (x < 0){
				x = 0;
				o.setVel(new Vector2d(-o.getVel().x, o.getVel().y));
			} else if (x > xb){
				x = xb;
				o.setVel(new Vector2d(-o.getVel().x, o.getVel().y));
			}
			if (y < 0){
				y = 0;
				o.setVel(new Vector2d(o.getVel().x, -o.getVel().y));
			} else if (y > yb){
				y = yb;
				o.setVel(new Vector2d(o.getVel().x, -o.getVel().y));
			}
			
			o.setPos(new Vector2d(x, y));
		}
//		System.out.println("end update task on thread "+Thread.currentThread().getName());
		latch.countDown();
	}
	
}
