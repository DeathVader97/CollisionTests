package de.FelixPerko.CollisionTest;

public class TickHelper {
	
	public static void moveObjects(double timeFactor){
		
		double xb = CollisionTestMain.bounds.x;
		double yb = CollisionTestMain.bounds.y;
		
		for (TestObject o : CollisionTestMain.objects){
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
	}
}
