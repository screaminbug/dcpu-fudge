package hr.tstrelar.dcpu.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import hr.tstrelar.dcpu.hardware.LEM1802;
import hr.tstrelar.dcpu.hardware.Monitor;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class MonitorWindow extends JFrame {
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1962679087856245705L;
	private static MonitorWindow mWindow;
	
	private static Monitor monitor;
	private int xSize;
	private int ySize;
	private final JLabel[] cells = new JLabel[384];
		
	public static MonitorWindow instance(final Monitor monitor) {
	    MonitorWindow.monitor = monitor;
	    
		if (mWindow == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new MonitorWindow();
					
				}
			});		
		}
		return mWindow;
	}
	
	public static void destroy() {
		mWindow.dispose();
		mWindow = null;
		
	}
	
	public MonitorWindow() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		xSize = monitor.getXSize();
		ySize = monitor.getYSize();
		
		setName(monitor.getFriendlyName());
		setTitle(monitor.getFriendlyName());
		setSize(1280, 960);
		
		setLayout(new GridLayout(12, 32));
		for (int i = 0; i < 384; i++) {
			cells[i] = new JLabel();
			add(cells[i]);
		}
		//cells[0].setText("PLEASE WAIT!");
		
		setVisible(true);
		
		
		new Thread (new Runnable() {
			
			@Override
			public void run() {
				if (monitor instanceof LEM1802) {
					BufferedImage[] screen = monitor.getScreen();
					BufferedImage bi;
					while(!Thread.currentThread().isInterrupted()) {
						for (int i = 0; i < 384; i++) {
							bi = screen[i];
							
							if (bi!= null) {
								BufferedImage scaled = new BufferedImage(40, 80, BufferedImage.TYPE_INT_ARGB);
								AffineTransform at = new AffineTransform();
								at.scale(10.0, 10.0);
								AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
								scaled = scaleOp.filter(bi, scaled);
								cells[i].setIcon(new ImageIcon(scaled));
								
							}
							
						}
					}
				}
			}
		}).start();
		
		
		
	}
}
