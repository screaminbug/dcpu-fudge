package hr.tstrelar.dcpu.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import hr.tstrelar.dcpu.hardware.LEM1802;
import hr.tstrelar.dcpu.hardware.Monitor;

import javax.swing.JFrame;
import javax.swing.JLabel;
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
		xSize = monitor.getXSize();
		ySize = monitor.getYSize();
		setSize(640, 480);
		setName(monitor.getFriendlyName());
		setTitle(monitor.getFriendlyName());
		final JLabel label = new JLabel();
		label.setText("Booting up...");
		add(label);
		setVisible(true);
		new Thread (new Runnable() {
			
			@Override
			public void run() {
				if (monitor instanceof LEM1802)
					try {
						Thread.sleep(1000);
						label.setText("LEM1802 IS ONLINE!!!"); //LEM1802 takes about 1 second to start up
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
			}
		}).start();
		
		
		
	}
	
//	private class BitmapConstructor extends SwingWorker<Queue<ColoredRectangle>, Object> {
//		private StringBuilder b = new StringBuilder();
//		
//		@Override
//		protected Queue<ColoredRectangle> doInBackground() {
//			Queue<ColoredRectangle> rectQueue = new ArrayDeque<>();
//						
//			Dimension rectSize = displayPanel.getSize();
//		    Insets rectInsets = displayPanel.getInsets();
//   
//		    double h =  (rectSize.height - rectInsets.top - rectInsets.bottom) / (double) ySize; 
//		    double w =  (rectSize.width - rectInsets.left - rectInsets.right) / (double) xSize;			
//
//			for (int i = 0; i < xSize * ySize; i++) {
//				double yPos = ((i)  % ySize) * h; 
//				double xPos = ((i) / ySize) * w;
//				
//				if (data.get(i)) {
//					rectQueue.add(new ColoredRectangle());
//				}		
//			}
//		
//			return rectQueue;
//		}
//		
//		@Override
//		protected void done() {
//			try {
//				if (get() == null) {
//					positionsArea.setText("Invalid parameter");
//				} else {				
//					width = (int)controlY.getValue();
//					controlX.setValue(new Integer(height));
//					positionsArea.setText(b.toString());
//					displayPanel.setRect(get());
//					displayPanel.repaint();
//				}
//				readyForBackground = true;	
//				
//			} catch (InterruptedException | ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}


}
