package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import de.FelixPerko.CollisionTest.Point;
import de.FelixPerko.CollisionTest.TickHelper;

public class SAPGrid {
	
	/*
	 * A grid of Sweep And Prune components.
	 * Decreases unnecessary overlap tests with distant objects.
	 * Improves insertion/deletion performance due to smaller lists.
	 * 
	 * AABB's are required to be smaller or as small as the cells in the current implementation.
	 */
	
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
		
		//calculate grid positions from corners
		int minX = (int)(b.xMin.value/factorX);
		int maxX = (int)(b.xMax.value/factorX);
		int minY = (int)(b.yMin.value/factorY);
		int maxY = (int)(b.yMax.value/factorY);
		int minYScale = minY*w;
		int maxYScale = maxY*w;
		
		//calculate grid indices
		int[] newSAPs = new int[4];
		newSAPs[0] = minX+minYScale;
		newSAPs[1] = maxX+minYScale;
		newSAPs[2] = minX+maxYScale;
		newSAPs[3] = maxX+maxYScale;
		
		//disable invalid indices
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
		
		//return if nothing changed compared to last tick
		if (newSAPs[0] == b.saps[0] && newSAPs[1] == b.saps[1] && newSAPs[2] == b.saps[2] && newSAPs[3] == b.saps[3])
			return;
		
		//add to new and remove from old sweep and prune components
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
	int threadCount = 0;
	HelperRunnable[] runnables;
	double higherTimingBorder = 0;
	
	public void tick() {
//		Arrays.stream(saps).parallel().forEach(s -> s.update());
		if (threadCount != TickHelper.helperThreadCount){
			HelperRunnable.sap = saps;
			threadCount = TickHelper.helperThreadCount;
			runnables = new HelperRunnable[threadCount];
			int elementsLeft = saps.length;
			for (int i = 0 ; i < threadCount ; i++){
				runnables[i] = new HelperRunnable();
				runnables[i].processAmount = Math.max(1, (int)((double)elementsLeft/(threadCount-i)));
			}
			higherTimingBorder = 1./threadCount + 1.5/saps.length;
		}
		CountDownLatch latch = new CountDownLatch(threadCount);
		HelperRunnable.latch = latch;
		
		double totalTime = 0;
		for (int i = 0 ; i < threadCount ; i++){
			long time = runnables[i].runningTime;
			totalTime += time;
		}
		
		int elementCounter = 0;
		int size = saps.length;
		if (totalTime == 0){
//			Arrays.stream(saps).parallel().forEach(s -> s.update());
			int elementsLeft = size;
			for (int i = 0 ; i < threadCount ; i++){
				HelperRunnable hr = runnables[i];
				int amount = (int) Math.round((double)elementsLeft/(threadCount-1));
				hr.processAmount = amount;
				elementsLeft -= amount;
				hr.setLoad(elementCounter, (elementCounter += amount));
				es.execute(hr);
			}
		}else {
			
			//rebalance lowest and highest
			int lowestPos = 0;
			int highestPos = lowestPos;
			long lowest = runnables[lowestPos].runningTime;
			long highest = runnables[lowestPos].runningTime;
			for (int j = lowestPos+1; j < threadCount ; j++){
				long time = runnables[j].runningTime;
				if (time < lowest){
					lowest = time;
					lowestPos = j;
				} else if (time > highest && runnables[j].processAmount > 1){
					highest = time;
					highestPos = j;
				}
			}
			runnables[lowestPos].processAmount++;
			runnables[highestPos].processAmount--;
			
			for (int i = 0 ; i < threadCount ; i++){
				HelperRunnable hr = runnables[i];
				int higherBorder = elementCounter + hr.processAmount;
				if (i == threadCount-1){
					higherBorder = size-1;
				}
				hr.setLoad(elementCounter, higherBorder);
				elementCounter = higherBorder;
//				System.out.println(hr.h-hr.l +" ("+(hr.averageRunningTime/totalTime)+")");
//				System.out.print(((hr.runningTime/totalTime)+"; ").replace('.', ','));
				es.execute(hr);
			}
//			System.out.println();
//			System.out.println("---"+higherTimingBorder);
		}
		
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
	int processAmount;
	
	public static SAP[] sap;
	public static CountDownLatch latch;
	public long runningTime;
	
	public void setLoad(int l, int h){
		this.l = l;
		this.h = h;
	}
	
	@Override
	public void run() {
		long t1 = System.nanoTime();
		for (int i = l ; i < h ; i++)
			sap[i].update();
		long t2 = System.nanoTime();
		runningTime = t2-t1;
		latch.countDown();
	}
}
