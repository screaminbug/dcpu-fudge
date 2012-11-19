package hr.tstrelar.dcpu.gui;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class ColoredRectangle {
	public Color color;
	public Rectangle2D.Double rectangle;
	
	public ColoredRectangle(Color color, Rectangle2D.Double rectangle) {
		this.color = color;
		this.rectangle = rectangle;
	}
}
