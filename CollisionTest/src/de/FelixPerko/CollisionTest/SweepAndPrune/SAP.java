package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.FelixPerko.CollisionTest.Point;

public class SAP {
	
	ArrayList<EndPoint> x,y;
	HashMap<Integer,HashMap<Integer,Byte>> intersections = new HashMap<>();
	
	ArrayList<EndPoint> addListX = new ArrayList<>();
	ArrayList<EndPoint> addListY = new ArrayList<>();
	ArrayList<EndPoint> removeListX = new ArrayList<>();
	ArrayList<EndPoint> removeListY = new ArrayList<>();
	
	Comparator<EndPoint> comp = new Comparator<EndPoint>() {
		public int compare(EndPoint o1, EndPoint o2) {
			if (o1.value < o2.value)
				return -1;
			else if (o1.value > o2.value)
				return 1;
			return 0;
		}
	};
	
	/*
	 * intersection byte:
	 * index 0 = intersects x
	 * index 1 = intersects y
	 */
	
	
	public SAP(int capacity){
		x = new ArrayList<>(capacity);
		y = new ArrayList<>(capacity);
	}
	
	public synchronized void addObject(EndPointOwner epo) {
		if (epo instanceof Point){
			Point p = (Point)epo;
			addListX.add(p.x);
			addListY.add(p.y);
		} else {
			Box b = (Box)epo;
			addListX.add(b.xMin);
			addListX.add(b.xMax);
			addListY.add(b.yMin);
			addListY.add(b.yMax);
		}
	}
	
	public synchronized void removeObject(EndPointOwner epo){
		if (epo instanceof Point){
			Point p = (Point)epo;
			removeListX.add(p.x);
			removeListY.add(p.y);
		} else {
			Box b = (Box)epo;
			removeListX.add(b.xMin);
			removeListX.add(b.xMax);
			removeListY.add(b.yMin);
			removeListY.add(b.yMax);
		}
	}
	
	public void update(){
		long t1 = System.nanoTime();
		if (!addListX.isEmpty()){
			Collections.sort(addListX, comp);
			Collections.sort(addListY, comp);
		}
		if (!removeListY.isEmpty()){
			Collections.sort(removeListX, comp);
			Collections.sort(removeListY, comp);
		}
		long t2 = System.nanoTime();
		insertNewObjects(x, addListX, true);
		insertNewObjects(y, addListY, false);
		removeObjects(x, removeListX, true);
		removeObjects(y, removeListY, false);
		long t3 = System.nanoTime();
		updateList(x,true);
		updateList(y,false);
		long t4 = System.nanoTime();
//		double tg = t4-t1;
//		if (((t4-t1)/100000)/10.0 > 30){
//			System.out.println("total time: "+(((t4-t1)/100000)/10.0+" for "+x.size()));
//			System.out.println((t2-t1)/tg+", "+(t3-t2)/tg+", "+(t4-t3)/tg);
//		}
//		System.exit(0);
	}
	
	ArrayList<EndPointOwner> removeIDs = new ArrayList<>();
	public boolean multiSapEnvironment = false;
	
	private void removeObjects(ArrayList<EndPoint> list, ArrayList<EndPoint> remove, boolean x) {
		if (remove.isEmpty())
			return;
		int nextIndex = 0;
		EndPoint nextPoint = remove.get(0); //next point to remove
		for (int i = 0 ; i < list.size() ; i++){ //iterate main list
			if (list.get(i) == nextPoint){ //is an object to remove
				list.remove(i);
				if (!x && nextPoint.status == 1){
					//remove collision record in other objects if needed
					Box box = (Box) nextPoint.owner;
					removeIDs.addAll(box.collisions.values());
					for (EndPointOwner epo : removeIDs){
						if (epo instanceof Box){
							if (multiSapEnvironment && !box.removed){
								if (epo instanceof Box){
									Box b1 = (Box)epo;
									Box b2 = box;
									if (!(b1.xMax.value < b2.xMin.value || b2.xMax.value < b1.xMin.value || b1.yMax.value < b2.yMin.value || b2.yMax.value < b1.yMin.value))
										continue;
								}
							}
							((Box)epo).collisions.remove(box.id);
							box.collisions.remove(((Box)epo).id);
						}
						
					}
					removeIDs.clear();
				}
				nextIndex++;
				if (nextIndex >= remove.size())
					break;
				nextPoint = remove.get(nextIndex);
				i--;
			}
		}
		if (nextIndex < remove.size()){
			list.removeAll(remove.subList(nextIndex, remove.size()));
		}
		remove.clear();
	}

	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<EndPoint> add, boolean x) {
		if (add.isEmpty())
			return;
		
		ArrayList<Box> openBoxes = new ArrayList<>(); //boxes that are open -> collide with inserted objects
		int nextIndex = 0;
		float nextValue = add.get(0).value; //value of object that needs to get inserted next
		boolean end = false;
		for (int i = 0 ; i < list.size() ; i++){ //iterate main list
			EndPoint p = list.get(i);
			if (p.value >= nextValue){ //found location to add next object
				EndPoint newP = add.get(nextIndex); //get the object to add
				if (!x){ //collision needs to be calculated (last axis)
					if (newP.status == 1) //is a min -> save to open collisions list
						openBoxes.add((Box)newP.owner);
					else if (newP.status == 0) //is a max -> remove from open collisions list
						openBoxes.remove((Box)newP.owner);
				}
				list.add(i,newP); //add the object
				//prepare next object
				nextIndex++;
				if (nextIndex >= add.size())
					end = true;
				else
					nextValue = add.get(nextIndex).value;
			}
			if (!x){ //add collision with new objects
				EndPointOwner epo = list.get(i).owner;
				for (Box b : openBoxes){
					if (b == epo)
						continue;
					if (epo instanceof Point){
						Point point = (Point)epo;
						if (point.x.value >= b.xMin.value && point.x.value <= b.xMax.value){
							b.collisions.putIfAbsent(point.id, point);
						}
					} else {
						Box b1 = (Box)epo;
						if (!(b1.xMax.value < b.xMin.value || b.xMax.value < b1.xMin.value)){
							b1.collisions.putIfAbsent((Integer)b.id, b);
							b.collisions.putIfAbsent((Integer)b1.id, b1);
						}
					}
				}
			}
			if (end)
				break;
		}
		for (int i = nextIndex ; i < add.size() ; i++){ //add remaining objects add the end of the list
			EndPoint newP = add.get(i);
			if (!x){
				if (newP.status == 1)
					openBoxes.add((Box)newP.owner);
				else if (newP.status == 0)
					openBoxes.remove((Box)newP.owner);
			}
			list.add(newP);
			if (!x){
				EndPointOwner epo = list.get(i).owner;
				for (Box b : openBoxes){
					if (b == epo)
						continue;
					if (epo instanceof Point){
						Point point = (Point)epo;
						if (point.x.value >= b.xMin.value && point.x.value <= b.xMax.value){
							b.collisions.putIfAbsent(point.id, point);
						}
					} else {
						Box b1 = (Box)epo;
						if (!(b1.xMax.value < b.xMin.value || b.xMax.value < b1.xMin.value)){
							b1.collisions.putIfAbsent((Integer)b.id, b);
							b.collisions.putIfAbsent((Integer)b1.id, b1);
						}
					}
				}
			}
		}
		add.clear();
		
//		if (x){
//			ArrayList<String> printlist = new ArrayList<>();
//			for (EndPoint e : list)
//				printlist.add(e.value+"");
//			System.out.println("insert to: "+printlist.toString());
//		}
	}

	public void updateList(ArrayList<EndPoint> list, boolean isXAxis){
		if (list.isEmpty())
			return;

//		if (isXAxis){
//			ArrayList<String> printlist = new ArrayList<>();
//			for (EndPoint e : list)
//				printlist.add(e.value+"");
//			System.out.println("update from: "+printlist.toString());
//		}
		
		int size = list.size();
		float lastValue = list.get(0).value;
		for (int i = 1 ; i < size ; i++){
			long t1 = System.nanoTime();
			EndPoint e = list.get(i);
			float value = e.value;
			if (lastValue <= value){
				lastValue = value;
				continue;
			}
			int nr = i;
			for (int j = i-1 ; j >= 0 ; j--){
				EndPoint e2 = list.get(j);
				if (e2.value <= value)
					break;
				list.set(nr, e2);
				list.set(j, e);
				nr = j;

				long tt1 = System.nanoTime();
				long tt2 = 0;
				int config = 0;
				//SWAP LOGIC:
				if (e.status == e2.status)
					continue;
				if (e.owner instanceof Point){
					Box b = (Box) e2.owner;
					Point p = (Point) e.owner;
					if (p.x.value >= b.xMin.value && p.x.value <= b.xMax.value && p.y.value >= b.yMin.value && p.y.value <= b.yMax.value){
						tt2 = System.nanoTime();
						b.collisions.putIfAbsent(p.id, p);
					} else {
						config = 1;
						tt2 = System.nanoTime();
						b.collisions.remove(p.id);
					}
				}
				else if (e2.owner instanceof Point){
					Box b = (Box) e.owner;
					Point p = (Point) e2.owner;
					if (p.x.value >= b.xMin.value && p.x.value <= b.xMax.value && p.y.value >= b.yMin.value && p.y.value <= b.yMax.value){
						config = 2;
						tt2 = System.nanoTime();
						b.collisions.putIfAbsent((Integer)p.id, p);
					} else {
						config = 3;
						tt2 = System.nanoTime();
						b.collisions.remove((Integer)p.id);
					}
				}
				else {
					Box b1 = (Box) e.owner;
					Box b2 = (Box) e2.owner;
					if (!(b1.xMax.value < b2.xMin.value || b2.xMax.value < b1.xMin.value || b1.yMax.value < b2.yMin.value || b2.yMax.value < b1.yMin.value)){
						config = 4;
						tt2 = System.nanoTime();
						b1.collisions.putIfAbsent((Integer)b2.id, b2);
						b2.collisions.putIfAbsent((Integer)b1.id, b1);
					} else {
						config = 5;
						tt2 = System.nanoTime();
						b1.collisions.remove((Integer)b2.id);
						b2.collisions.remove((Integer)b1.id);
					}
				}
				long tt3 = System.nanoTime();
//				if (tt3-tt1 > 50000000){
//					System.out.println(config);
//					if (config == 3)
//						System.out.println("objects: "+((Box)e.owner).collisions.size());
//					System.out.println((tt2-tt1)+", "+(tt3-tt2));
//				}
			}
			lastValue = value;
//			timings.add((int) (System.nanoTime()-t1));
		}
		
//		if (isXAxis){
//			ArrayList<String> printlist = new ArrayList<>();
//			for (EndPoint e : list)
//				printlist.add(e.value+"");
//			System.out.println("update to: "+printlist.toString());
//		}
//		if (System.nanoTime() - beginTime > 50000000)
//			for (int time : timings)
//				if (time > 100000)
//					System.out.println(time);
	}
}
