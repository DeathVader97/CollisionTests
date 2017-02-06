package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

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
	
	public void addObject(Box box) {
		addListX.add(box.xMin);
		addListX.add(box.xMax);
		addListY.add(box.yMin);
		addListY.add(box.yMax);
	}
	
	public void removeObject(Box box){
		removeListX.add(box.xMin);
		removeListX.add(box.xMax);
		removeListY.add(box.yMin);
		removeListY.add(box.yMax);
	}
	
	public void update(){
		long t1 = System.nanoTime();
		Collections.sort(addListX, comp);
		Collections.sort(addListY, comp);
		Collections.sort(removeListX, comp);
		Collections.sort(removeListY, comp);
		long t2 = System.nanoTime();
		removeObjects(x, removeListX, true);
		removeObjects(y, removeListY, false);
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

	private void removeObjects(ArrayList<EndPoint> list, ArrayList<EndPoint> remove, boolean x) {
		if (remove.isEmpty())
			return;
		int nextIndex = 0;
		EndPoint nextPoint = remove.get(0);
		for (int i = 0 ; i < list.size() ; i++){
			if (list.get(i) == nextPoint){
				list.remove(i);
				if (!x && !nextPoint.isMin){
					Box box = nextPoint.owner;
					for (Box b : box.collisions.values()){
						b.collisions.remove(box.id);
					}
				}
				nextIndex++;
				if (nextIndex >= remove.size())
					break;
				nextPoint = remove.get(nextIndex);
				i--;
			}
		}
		remove.clear();
	}

	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<EndPoint> add, boolean x) {
		if (add.isEmpty())
			return;
		ArrayList<Box> openBoxes = new ArrayList<>();
		int nextIndex = 0;
		float nextValue = add.get(0).value;
		boolean end = false;
		for (int i = 0 ; i < list.size() ; i++){
			EndPoint p = list.get(i);
			if (p.value >= nextValue){
				EndPoint newP = add.get(nextIndex);
				if (!x){
					if (newP.isMin)
						openBoxes.add(newP.owner);
					else
						openBoxes.remove(newP.owner);
				}
				list.add(newP);
				nextIndex++;
				if (nextIndex >= add.size())
					end = true;
				else
					nextValue = add.get(nextIndex).value;
			}
			if (!x){
				Box b1 = list.get(i).owner;
				for (Box b : openBoxes){
					if (b == b1)
						continue;
					boolean collides = !(b1.xMax.value < b.xMin.value || b.xMax.value < b1.xMin.value);
					if (collides){
						if (!b1.collisions.containsKey(b.id)){
							b1.collisions.put((Integer)b.id, b);
							b.collisions.put((Integer)b1.id, b1);
						}
					}
				}
			}
			if (end)
				break;
		}
		for (int i = nextIndex ; i < add.size() ; i++){
			EndPoint newP = add.get(i);
			if (!x){
				if (newP.isMin)
					openBoxes.add(newP.owner);
				else
					openBoxes.remove(newP.owner);
			}
			list.add(newP);
			if (!x){
				Box b1 = list.get(i).owner;
				for (Box b : openBoxes){
					if (b == newP.owner)
						continue;
					boolean collides = !(b1.xMax.value < b.xMin.value || b.xMax.value < b1.xMin.value);
					if (collides){
						if (!b1.collisions.containsKey(b.id)){
							b1.collisions.put((Integer)b.id, b);
							b.collisions.put((Integer)b1.id, b1);
						}
					}
				}
			}
		}
		add.clear();
	}

//	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<Box> add, boolean x) {
//		if (add.size() == 0)
//			return;
//		int addIndex = 0;
//		if (x){
//			double nextValue = add.get(addIndex).xMin.value;
//			for (int i = 0 ; i < list.size() ; i++){
//				if (list.get(i).value >= nextValue){
//					list.add(i, add.get(addIndex).xMin);
//					addIndex++;
//					nextValue = add.get(addIndex).xMin.value;
//				}
//			}
//			for (int i = addIndex ; i < add.size() ; i++){
//				list.add(add.get(i).xMin);
//			}
//			
//			addIndex = 0;
//			nextValue = add.get(addIndex).xMax.value;
//			for (int i = 0 ; i < list.size() ; i++){
//				if (list.get(i).value >= nextValue){
//					list.add(i, add.get(addIndex).xMax);
//					addIndex++;
//					nextValue = add.get(addIndex).xMax.value;
//				}
//			}
//			for (int i = addIndex ; i < add.size() ; i++){
//				list.add(add.get(i).xMax);
//			}
//		} else {
//			double nextValue = add.get(addIndex).yMin.value;
//			for (int i = 0 ; i < list.size() ; i++){
//				if (list.get(i).value >= nextValue){
//					list.add(i, add.get(addIndex).yMin);
//					addIndex++;
//					nextValue = add.get(addIndex).yMin.value;
//				}
//			}
//			for (int i = addIndex ; i < add.size() ; i++){
//				list.add(add.get(i).yMin);
//			}
//			
//			addIndex = 0;
//			nextValue = add.get(addIndex).yMax.value;
//			for (int i = 0 ; i < list.size() ; i++){
//				if (list.get(i).value >= nextValue){
//					list.add(i, add.get(addIndex).yMax);
//					addIndex++;
//					nextValue = add.get(addIndex).yMax.value;
//				}
//			}
//			for (int i = addIndex ; i < add.size() ; i++){
//				list.add(add.get(i).yMax);
//			}
//			
//			//ADD NEW OVERLAPS
//			addIndex = 0;
//			int id = add.get(addIndex).id;
//			ArrayList<Box> activeAdds = new ArrayList<>();
//			for (int i = 0 ; i < list.size() ; i++){
//				EndPoint e = list.get(i);
//				Box b1 = e.owner;
//				boolean remove = false;
//				for (Box b : activeAdds){
//					if (b.id == b1.id){
//						remove = true;
//						continue;
//					}
//					boolean collides = !(b1.xMax.value < b.xMin.value || b.xMax.value < b1.xMin.value);
//					if (collides){
//						if (!b1.collisions.containsKey(b.id)){
//							b1.collisions.put((Integer)b.id, true);
//							b.collisions.put((Integer)b1.id, true);
//						}
//					}
//				}
//				
//				if (remove)
//					activeAdds.remove(b1);
//				if (e.isMin && e.owner.id == id){
//					activeAdds.add(e.owner);
//					addIndex++;
//					if (addIndex >= add.size())
//						break;
//					id = add.get(addIndex).id;
//				}
//			}
//		}
//		add.clear();
//	}

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
				if (e.isMin == e2.isMin)
					continue;
				Box b1 = e.owner;
				Box b2 = e2.owner;
				boolean collides = !(b1.xMax.value < b2.xMin.value || b2.xMax.value < b1.xMin.value || b1.yMax.value < b2.yMin.value || b2.yMax.value < b1.yMin.value);
				if (collides){
					if (!b1.collisions.containsKey(b2.id)){
						b1.collisions.put((Integer)b2.id, b2);
						b2.collisions.put((Integer)b1.id, b1);
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
