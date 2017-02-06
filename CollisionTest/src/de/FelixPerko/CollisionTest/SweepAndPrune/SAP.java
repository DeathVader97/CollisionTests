package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.FelixPerko.CollisionTest.Vector2d;

public class SAP {
	
	ArrayList<EndPoint> x,y;
	HashMap<Integer,HashMap<Integer,Byte>> intersections = new HashMap<>();
	
	ArrayList<Box> addListX = new ArrayList<>();
	ArrayList<Box> addListY = new ArrayList<>();
	ArrayList<Box> removeListX = new ArrayList<>();
	ArrayList<Box> removeListY = new ArrayList<>();
	
	Comparator<Box> xComp = new Comparator<Box>() {
		public int compare(Box o1, Box o2) {
			if (o1.xMin.value < o2.xMin.value)
				return -1;
			else if (o1.xMin.value > o2.xMin.value)
				return 1;
			return 0;
		}
	};
	Comparator<Box> yComp = new Comparator<Box>() {
		public int compare(Box o1, Box o2) {
			if (o1.yMin.value < o2.yMin.value)
				return -1;
			else if (o1.yMin.value > o2.yMin.value)
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
	
	public void addObject(Box box) {
		addListX.add(box);
		addListY.add(box);
	}
	
	public void removeObject(Box box){
	}
	
	public void update(){
		long t1 = System.nanoTime();
		Collections.sort(addListX, xComp);
		Collections.sort(addListY, yComp);
		long t2 = System.nanoTime();
		insertNewObjects(x, addListX, true);
		insertNewObjects(y, addListY, false);
		long t3 = System.nanoTime();
		updateList(x,true);
		updateList(y,false);
		long t4 = System.nanoTime();
		double tg = t4-t1;
//		System.out.println("total time: "+((t4-t1)/1000000000.0+"s"));
//		System.out.println((t2-t1)/tg+", "+(t3-t2)/tg+", "+(t4-t3)/tg);
//		System.exit(0);
	}

	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<Box> add, boolean x) {
		if (add.size() == 0)
			return;
		int addIndex = 0;
		if (x){
			double nextValue = add.get(addIndex).xMin.value;
			for (int i = 0 ; i < list.size() ; i++){
				if (list.get(i).value >= nextValue){
					list.add(i, add.get(addIndex).xMin);
					addIndex++;
					nextValue = add.get(addIndex).xMin.value;
				}
			}
			for (int i = addIndex ; i < add.size() ; i++){
				list.add(add.get(i).xMin);
			}
			
			addIndex = 0;
			nextValue = add.get(addIndex).xMax.value;
			for (int i = 0 ; i < list.size() ; i++){
				if (list.get(i).value >= nextValue){
					list.add(i, add.get(addIndex).xMax);
					addIndex++;
					nextValue = add.get(addIndex).xMax.value;
				}
			}
			for (int i = addIndex ; i < add.size() ; i++){
				list.add(add.get(i).xMax);
			}
		} else {
			double nextValue = add.get(addIndex).yMin.value;
			for (int i = 0 ; i < list.size() ; i++){
				if (list.get(i).value >= nextValue){
					list.add(i, add.get(addIndex).yMin);
					addIndex++;
					nextValue = add.get(addIndex).yMin.value;
				}
			}
			for (int i = addIndex ; i < add.size() ; i++){
				list.add(add.get(i).yMin);
			}
			
			addIndex = 0;
			nextValue = add.get(addIndex).yMax.value;
			for (int i = 0 ; i < list.size() ; i++){
				if (list.get(i).value >= nextValue){
					list.add(i, add.get(addIndex).yMax);
					addIndex++;
					nextValue = add.get(addIndex).yMax.value;
				}
			}
			for (int i = addIndex ; i < add.size() ; i++){
				list.add(add.get(i).yMax);
			}
		}
		add.clear();
	}

	public void updateList(ArrayList<EndPoint> list, boolean isXAxis){
		
		int size = list.size();
		for (int i = 1 ; i < size ; i++){
			EndPoint e = list.get(i);
			float value = e.value;
			int nr = i;
			for (int j = i-1 ; j >= 0 ; j--){
				EndPoint e2 = list.get(j);
				if (e2.value <= value)
					break;
				list.set(nr, e2);
				list.set(j, e);
				nr = j;
				
				//SWAP LOGIC:
				Box b1 = e.owner;
				Box b2 = e2.owner;
				boolean collides = !(b1.xMax.value < b2.xMin.value || b2.xMax.value < b1.xMin.value || b1.yMax.value < b2.yMin.value || b2.yMax.value < b1.yMin.value);
				if (collides){
					if (!b1.collisions.containsKey(b2.id)){
						b1.collisions.put((Integer)b2.id, true);
						b2.collisions.put((Integer)b1.id, true);
					}
				} else {
					e.owner.collisions.remove((Integer)e2.owner.id);
					e2.owner.collisions.remove((Integer)e.owner.id);
				}
			}
		}
	}
	
	private byte getIntersectionByte(int id1, int id2){
		if (id1 > id2){
			int temp = id2;
			id2 = id1;
			id1 = temp;
		}
		HashMap<Integer, Byte> map = intersections.get(id1);
		if (map == null)
			return (byte)0;
		Byte b = map.get(id2);
		if (b == null)
			return (byte)0;
		return b;
	}
	
	private void setIntersectionByte(int id1, int id2, byte set){
		if (set == 0){
			HashMap<Integer, Byte> map = intersections.get(id1);
			if (map != null){
				map.remove(id2);
			}
		} else {
			HashMap<Integer, Byte> map = intersections.get(id1);
			if (map == null){
				map = new HashMap<>();
				intersections.put(id1, map);
				map.put(id2, set);
				return;
			}
			if (map.containsKey(id2))
				map.remove(id2);
			map.put(id2, set);
		}
	}
}
