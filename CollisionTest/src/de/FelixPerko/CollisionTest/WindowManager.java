package de.FelixPerko.CollisionTest;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ConcurrentModificationException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class WindowManager {
	
	JFrame frame;
	CustomComponent c;
	boolean disabled = true;
	
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
	
	@Override
	protected void paintComponent(Graphics g) {
		double rad = CollisionTestMain.collisionDistance;
		try{
			for (TestObject o : CollisionTestMain.objects){
				if (o.SAPbox.collisions.isEmpty())
					g.setColor(Color.BLACK);
				else
					g.setColor(Color.RED);
				g.drawRect((int)(o.pos.x-rad), (int)(o.pos.y-rad), (int)rad*2, (int)rad*2);
			}
		} catch (ConcurrentModificationException e){
			paintComponent(g);
		}
	}
}
