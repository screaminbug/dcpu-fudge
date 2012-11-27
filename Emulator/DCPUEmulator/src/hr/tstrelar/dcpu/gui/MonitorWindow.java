package hr.tstrelar.dcpu.gui;

import hr.tstrelar.dcpu.hardware.Monitor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MonitorWindow extends JFrame {

	private static final long serialVersionUID = -8968538982081989471L;
	private static final int LEFT_RIGHT_BORDER_SIZE = 120;
	private static final int TOP_BOTTOM_BORDER_SIZE = 120;
	private static final int SCALE_FACTOR = 4;
	
	private static MonitorWindow mWindow;
	private static Monitor monitor;
	
	private final Timer screenTimer = new Timer(32, new BackDrawer());
	
	private BufferedImage img;

	private Timer delayTimer; 

	private JPanel pane = new JPanel();
	private JLabel bootMsg = new JLabel("Starting the screen. Please wait...");
	private JComponent drawSurface = new DrawSurface();
	
	private int monitorWidth = monitor.getXSize() * SCALE_FACTOR + TOP_BOTTOM_BORDER_SIZE;
	private int monitorHeight = monitor.getYSize() * SCALE_FACTOR + LEFT_RIGHT_BORDER_SIZE;
	
	

	public static MonitorWindow instance(final Monitor monitor) {
		MonitorWindow.monitor = monitor;
		

		if (mWindow == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mWindow = new MonitorWindow();

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setName(monitor.getFriendlyName());
		setTitle(monitor.getFriendlyName());
					
		drawSurface.add(bootMsg);
		add(drawSurface);
		
        setResizable(false);
        
		setVisible(true);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int width = monitor.getXSize() * SCALE_FACTOR + getInsets().left + getInsets().right + LEFT_RIGHT_BORDER_SIZE/2;
		int height = monitor.getYSize() * SCALE_FACTOR + getInsets().top + getInsets().bottom + TOP_BOTTOM_BORDER_SIZE/2;
		int x = screen.width / 2 - width / 2;
		int y = screen.height / 2 - height / 2; 
		setBounds(x, y, width, height);
		
		delayTimer = new Timer(800, new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent arg0) {
				drawSurface.remove(bootMsg);
				screenTimer.start();
				delayTimer.stop();
			}
		});
		delayTimer.start();
		
				
        
	}
	
	private class DrawSurface extends JPanel {
		
		private static final long serialVersionUID = 7547367649380027361L;

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(img, 0, 0, this);
		}
	}

	
	private class BackDrawer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent dummy) {
						
			img = new BufferedImage(monitorWidth, monitorHeight, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2d = img.createGraphics();
			int yOffset = TOP_BOTTOM_BORDER_SIZE / (4 * SCALE_FACTOR);
			int xOffset = LEFT_RIGHT_BORDER_SIZE / (4 * SCALE_FACTOR);
			g2d.setColor(monitor.getBorderColor());
			g2d.fillRect(0, 0, monitorWidth + xOffset, monitorHeight + yOffset);
			int i = 0;
			
			for (int y = yOffset; y < monitor.getYSize() + yOffset; y += 8) {
				for (int x = xOffset; x < monitor.getXSize() + xOffset; x += 4) {
					g2d.drawImage(monitor.getCell(i++), x, y, null);
				}
			}

			BufferedImage after = new BufferedImage(monitorWidth, monitorHeight, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(SCALE_FACTOR, SCALE_FACTOR);
			AffineTransformOp scaleOp = 
			   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			img = scaleOp.filter(img, after);
			
			mWindow.repaint();
			
		}
		
	}
	
	
}
