package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import de.FelixPerko.CollisionTest.Point;
import de.FelixPerko.CollisionTest.TickHelper;

public class SAPGrid {
	
	public SAP[] saps;
	int w,h;
	double factorX,factorY;
	
	public SAPGrid(int gridW, int gridH, int initialCapacity, double worldW, double worldH) {
		this.w = gridW;
		this.h = gridH;
		this.factorX = worldW/gridW;
		this.factorY = worldH/gridW;
		this.saps = new SAP[gridW*gridH];
		for (int i = 0 ; i < saps.length ; i++){
			saps[i] = new SAP(initialCapacity);
			saps[i].multiSapEnvironment = true;
		}
	}
	
	boolean test = true;
	
	
	public void updatePos(Box b){
		long t1 = System.nanoTime();
		int minX = (int)(b.xMin.value/factorX);
		int maxX = (int)(b.xMax.value/factorX);
		int minY = (int)(b.yMin.value/factorY);
		int maxY = (int)(b.yMax.value/factorY);
		int minYScale = minY*w;
		int maxYScale = maxY*w;
		
		int[] newSAPs = new int[4];
		newSAPs[0] = minX+minYScale;
		newSAPs[1] = maxX+minYScale;
		newSAPs[2] = minX+maxYScale;
		newSAPs[3] = maxX+maxYScale;
		if (minX < 0){
			newSAPs[0] = -1;
			newSAPs[2] = -1;
		}
		if (maxX >= w){
			newSAPs[1] = -1;
			newSAPs[3] = -1;
		}
		if (minY < 0){
			newSAPs[0] = -1;
			newSAPs[1] = -1;
		}
		if (maxY >= h){
			newSAPs[2] = -1;
			newSAPs[3] = -1;
		}
		if (newSAPs[0] == b.saps[0] && newSAPs[1] == b.saps[1] && newSAPs[2] == b.saps[2] && newSAPs[3] == b.saps[3])
			return;
		
		ArrayList<Integer> added = new ArrayList<>();
		ArrayList<Integer> removed = new ArrayList<>();
		for (int i = 0 ; i < 4 ; i++){
			if (b.saps[i] != newSAPs[i]){
				if (newSAPs[i] != -1){
					int val = newSAPs[i];
					if (!added.contains(val) && !contains(b.saps, val)){
						saps[val].addObject(b);
						added.add(val);
					}
				}
				if (b.saps[i] != -1){
					int val = b.saps[i];
					if (!removed.contains(val) && !contains(newSAPs, val)){
						saps[val].removeObject(b);
						removed.add(val);
					}
				}
			}
		}
		b.saps = newSAPs;
	}

	public void updatePos(Point point) {
		int n = (int)(point.x.value/factorX)+(int)(point.y.value/factorY)*w;
		if (n == point.saps[0])
			return;
		else {
			if (n != -1)
				saps[n].addObject(point);
			if (point.saps[0] != -1)
				saps[point.saps[0]].removeObject(point);
		}
		point.saps[0] = n;
	}
	
	private boolean contains(int[] arr, int value){
		for (int i = 0 ; i < arr.length ; i++){
			if (arr[i] == value)
				return true;
		}
		return false;
	}
	
	int last = 0;
	
	ExecutorService es = TickHelper.es;
	int threadCount = TickHelper.helperThreadCount;
	HelperRunnable[] runnables = new HelperRunnable[threadCount];
	{
		for (int i = 0 ; i < threadCount ; i++)
			runnables[i] = new HelperRunnable();
	}
	
	public void tick() {
//		int totalObjects = 0;
//		for (SAP sap : saps){
//			totalObjects += sap.x.size();
//		}
//		System.out.println(totalObjects);
		
		
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		int s = saps.length;
		int blockSize = s/(threadCount);
		
		HelperRunnable.sap = saps;
		HelperRunnable.latch = latch;
		for (int i = 0 ; i < threadCount-1 ; i++){
			HelperRunnable run = runnables[i];
			run.setLoad(i*blockSize, (i+1)*blockSize);
			es.execute(run);
		}
		HelperRunnable run = runnables[runnables.length-1];
		run.setLoad((threadCount-1)*blockSize, s);
		es.execute(run);
		
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int[] findBordersX(){
		int[] res = new int[w];
		for (int i = 0 ; i < w ; i++){
			res[i] = (int)(i*factorX);
		}
		return res;
	}
	
	public int[] findBordersY(){
		int[] res = new int[h];
		for (int i = 0 ; i < h ; i++){
			res[i] = (int)(i*factorY);
		}
		return res;
	}
}


class HelperRunnable implements Runnable{
	
	int l,h;
	public static SAP[] sap;
	public static CountDownLatch latch;
	
	public static AtomicInteger nextIndex = new AtomicInteger(0);
	
	public void setLoad(int l, int h){
		this.l = l;
		this.h = h;
	}
	
	@Override
	public void run() {
		for (int i = l ; i < h ; i++)	
			sap[i].update();
		latch.countDown();
	}
}
