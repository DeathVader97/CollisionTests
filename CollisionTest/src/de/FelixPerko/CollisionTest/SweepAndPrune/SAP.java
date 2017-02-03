package de.FelixPerko.CollisionTest.SweepAndPrune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SAP {
	
	ArrayList<EndPoint> x,y;
	HashMap<Integer,HashMap<Integer,Byte>> intersections = new HashMap<>();
	
	ArrayList<Box> addListX = new ArrayList<>();
	ArrayList<Box> addListY = new ArrayList<>();
	
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
//		x.add(box.xMin);
//		x.add(box.xMax);
//		y.add(box.yMin);
//		y.add(box.yMax);
	}
	
	public void removeObject(Box box){
	}
	
	public void update(){
		Collections.sort(addListX, xComp);
		Collections.sort(addListY, yComp);
		
		insertNewObjects(x, addListX, true);
		insertNewObjects(y, addListY, false);
		
		updateList(x,true);
		updateList(y,false);
	}
	
	private void insertNewObjects(ArrayList<EndPoint> list, ArrayList<Box> add, boolean x) {
		int j = 0; //minPos
		int j2 = 0; //maxPos
		for (Box b : add){
//			System.out.println();
//			for (int i = 0 ; i < list.size() ; i++)
//				System.out.print(list.get(i).owner.id+":"+(int)list.get(i).value+", ");
			//add min
			if (x){
				if (j >= list.size())
					list.add(b.xMin);
				else {
					while (j < list.size() && (b.xMin.value > list.get(j).value))
						j++;
					list.add(j,b.xMin);
				}
				j++;
			} else {
				if (j >= list.size())
					list.add(b.xMin);
				else {
					while (j < list.size() && (!list.get(j).isMin || b.yMin.value > list.get(j).value))
						j++;
					list.add(j,b.yMin);
					j++;
				}
			}
			
			//add max
			if (j2 <= j)
				j2 = j+1;
			int setMaxTo = list.size();
			if (j2 >= list.size())
				if (x)
					list.add(b.xMax);
				else
					list.add(b.xMin);
			else {
				while (j2 < list.size() && list.get(j2).isMin)
					j2++;
				if (x){
					if (j2 >= list.size())
						list.add(b.xMax);
					else {
						while (j2+1 < list.size() && b.xMax.value >= list.get(j2+1).value){
							j2++;
							while (list.get(j2).isMin)
								j2++;
						}
						if (b.xMax.value >= list.get(j2-1).value && b.xMax.value <= list.get(j2).value){
							list.add(j2, b.xMax);
							setMaxTo = j2;
							j2++;
						} else {
							list.add(b.xMax);
							setMaxTo = list.size();
						}
					}
				} else {
					if (j2 >= list.size())
						list.add(b.xMax);
					else {
						while (j2+1 < list.size() && b.yMax.value >= list.get(j2+1).value){
							j2++;
							while (list.get(j2).isMin)
								j2++;
						}
						if (b.yMax.value >= list.get(j2-1).value && b.yMax.value <= list.get(j2).value){
							list.add(j2, b.yMax);
							setMaxTo = j2;
							j2++;
						} else {
							setMaxTo = list.size();
							list.add(b.yMax);
						}
					}
				}
			}
			
			//update intersections
			int index = 0;
			if (!x)
				index = 1;
			for (int i = j ; i < setMaxTo ; i++){
				byte data = getIntersectionByte(list.get(i).owner.id, b.id);
				if (((data >> index) & 1) == 0){
					if (index == 0)
						data++;
					else
						data += 2;
					setIntersectionByte(list.get(i).owner.id, b.id, data);
				}
			}
		}
		add.clear();
	}

	public void updateList(ArrayList<EndPoint> list, boolean isXAxis){
		int index = 0;
		if (!isXAxis)
			index = 1;
		
		int swaps = 0;
		
		int size = list.size();
		for (int i = 0 ; i < size ; i++){
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
				
				swaps++;
				
				//SWAP LOGIC:
				if (e.isMin && !e2.isMin){//goes Inside
					byte b = getIntersectionByte(e.owner.id, e2.owner.id);
					if (((b >> index) & 1) == 0){
						if (index == 0)
							b++;
						else
							b += 2;
						setIntersectionByte(e.owner.id, e2.owner.id, b);
					}
				} else if (!e.isMin && e2.isMin){//goes Outside
					byte b = getIntersectionByte(e.owner.id, e2.owner.id);
					if (((b >> index) & 1) == 1){
						if (index == 0)
							b--;
						else
							b -= 2;
						setIntersectionByte(e.owner.id, e2.owner.id, b);
					}
				}
			}
//			if (i != nr)
//				System.out.println("swapped from "+i+" to "+nr);
		}

//		System.out.println();
//		for (int i = 0 ; i < list.size() ; i++)
//			System.out.print(list.get(i).owner.id+":"+(int)list.get(i).value+", ");
//		System.out.println(swaps);
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
