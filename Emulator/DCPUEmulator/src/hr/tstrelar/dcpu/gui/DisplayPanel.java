package hr.tstrelar.dcpu.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Queue;
import java.util.Random;

import javax.swing.JComponent;

public class DisplayPanel extends JComponent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3212535335401964850L;
	
	private Queue<ColoredRectangle> rectQueue;
		
	public void setRect(Queue<ColoredRectangle> rectlist) {
		this.rectQueue = rectlist;
	}

	public void paintComponent(Graphics g) {
	      if (rectQueue == null) return;
	      Graphics2D g2d = (Graphics2D) g;
  	      
	      ColoredRectangle rect;
	      while ((rect = rectQueue.poll()) != null) {
	    	  g2d.setColor(rect.color);
	    	  g2d.fill(rect.rectangle);
	      }
		  


	}

}