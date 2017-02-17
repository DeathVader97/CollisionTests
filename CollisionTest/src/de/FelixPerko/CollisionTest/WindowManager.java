package de.FelixPerko.CollisionTest;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.FelixPerko.CollisionTest.CollisionTests.CollisionTest;
import de.FelixPerko.CollisionTest.CollisionTests.SweepAndPruneGridTest;

public class WindowManager {
	
	JFrame frame;
	CustomComponent c;
	boolean disabled;
	
	public WindowManager(boolean enabled){
		this.disabled = !enabled;
	}
	
	public void init() {
		if (disabled)
			return;
		frame = new JFrame();
		frame.getContentPane().setPreferredSize(new Dimension(1000, 1000));
		c = new CustomComponent();
		frame.add(c);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void update() {
		if (disabled)
			return;
		frame.repaint();
	}
	
}
class CustomComponent extends JComponent{
	private static final long serialVersionUID = -2455429557528511279L;
	
	private static final Color DARK_GREEN = new Color(0f, 0.5f, 0f);
	
	@Override
	protected void paintComponent(Graphics g) {
		double rad = CollisionTestMain.collisionDistance;
			CollisionTest test = CollisionTestMain.currentTest;
			if (test != null && test instanceof SweepAndPruneGridTest){
				g.setColor(Color.GRAY);
				SweepAndPruneGridTest sapgt = (SweepAndPruneGridTest) test;
				if (sapgt.grid != null){
					for (int x : sapgt.grid.findBordersX()){
						g.drawLine(x, 0, x, (int)CollisionTestMain.bounds.y);
					}
					for (int y : sapgt.grid.findBordersY()){
						g.drawLine(0, y, (int)CollisionTestMain.bounds.x, y);
					}
				}
			}
			for (TestObject o : new ArrayList<TestObject>(CollisionTestMain.objects)){
				try{
					if (o instanceof StaticPointObject){
						g.setColor(DARK_GREEN);
						g.drawRect((int)(o.pos.x), (int)(o.pos.y), 1, 1);;
					} else {
						if (((DynamicDimentionalObject)o).SAPbox.collisions.isEmpty())
							g.setColor(Color.BLACK);
						else
							g.setColor(Color.RED);
						g.drawRect((int)(o.pos.x-rad), (int)(o.pos.y-rad), (int)rad*2, (int)rad*2);
					}
				} catch (ConcurrentModificationException|NullPointerException e){
					paintComponent(g);
				}
			}
	}
}
