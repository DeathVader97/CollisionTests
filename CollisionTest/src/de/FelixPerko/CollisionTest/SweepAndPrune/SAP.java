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
		//Sort the insertion/deletion lists
		if (!addListX.isEmpty()){
			Collections.sort(addListX, comp);
			Collections.sort(addListY, comp);
		}
		if (!removeListY.isEmpty()){
			Collections.sort(removeListX, comp);
			Collections.sort(removeListY, comp);
		}
		long t2 = System.nanoTime();
		//perform insertions/deletions
		insertNewObjects(x, addListX, true);
		insertNewObjects(y, addListY, false);
		removeObjects(x, removeListX, true);
		removeObjects(y, removeListY, false);
		long t3 = System.nanoTime();
		//sort lists and update collisions
		updateList(x,true);
		updateList(y,false);
		long t4 = System.nanoTime();
//		double tg = t4-t1;
//		if (((t4-t1)/100000)/10.0 > 30){
//			System.out.println("total time: "+(((t4-t1)/100000)/10.0+" for "+x.size()));
//			System.out.println((t2-t1)/tg+", "+(t3-t2)/tg+", "+(t4-t3)/tg);
//		}
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
	
	ArrayList<Box> openBoxes = new ArrayList<Box>(); //boxes that are open -> collide with objects

	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<EndPoint> add, boolean x) {
		if (add.isEmpty())
			return;
		
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
							if (b1.collisions.putIfAbsent((Integer)b.id, b) == null)
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
							if (b1.collisions.putIfAbsent(b.id, b) == null)
								b.collisions.putIfAbsent(b1.id, b1);
						}
					}
				}
			}
		}
		openBoxes.clear();
		add.clear();
	}

	public void updateList(ArrayList<EndPoint> list, boolean isXAxis){
		if (list.isEmpty())
			return;
		
		int size = list.size();
		float lastValue = list.get(0).value;
		for (int i = 1 ; i < size ; i++){
			EndPoint e = list.get(i);
			float value = e.value;
			if (lastValue <= value){//last value was smaller or equivalent to current -> still sorted
				lastValue = value;
				continue;
			}
			int nr = i;
			for (int j = i-1 ; j >= 0 ; j--){
				EndPoint e2 = list.get(j);
				if (j == i-1)
					lastValue = e2.value;
				else if (e2.value <= value)
					break;
				//swap objects
				list.set(nr, e2);
				list.set(j, e);
				nr = j;
				
				//overlap tests
				if (e.status == e2.status)
					continue;
				if (e.owner instanceof Point){
					Box b = (Box) e2.owner;
					Point p = (Point) e.owner;
					if (p.x.value >= b.xMin.value && p.x.value <= b.xMax.value && p.y.value >= b.yMin.value && p.y.value <= b.yMax.value){
						b.collisions.putIfAbsent(p.id, p);
					} else {
						b.collisions.remove(p.id);
					}
				}
				else if (e2.owner instanceof Point){
					Box b = (Box) e.owner;
					Point p = (Point) e2.owner;
					if (p.x.value >= b.xMin.value && p.x.value <= b.xMax.value && p.y.value >= b.yMin.value && p.y.value <= b.yMax.value){
						b.collisions.putIfAbsent(p.id, p);
					} else {
						b.collisions.remove(p.id);
					}
				}
				else {
					Box b1 = (Box) e.owner;
					Box b2 = (Box) e2.owner;
					if (!(b1.xMax.value < b2.xMin.value || b2.xMax.value < b1.xMin.value || b1.yMax.value < b2.yMin.value || b2.yMax.value < b1.yMin.value)){
						if (b1.collisions.putIfAbsent(b2.id, b2) == null)
							b2.collisions.putIfAbsent(b1.id, b1);
					} else {
						if (b1.collisions.remove(b2.id) != null)
							b2.collisions.remove(b1.id);
					}
				}
			}
		}
	}
}
