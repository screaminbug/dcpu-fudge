package hr.tstrelar.dcpu.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import hr.tstrelar.dcpu.hardware.Keyboard;

public class KeyboardWindow extends JFrame {

	private static KeyboardWindow kWindow;
	
	public static KeyboardWindow instance(final Keyboard keyboard) {
		
		if (kWindow == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new KeyboardWindow(keyboard);
				}
			});		
		}
		return kWindow;
	}
		
	private KeyboardWindow(final Keyboard keyboard) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(320, 200);
		setName(keyboard.getFriendlyName());
		setTitle(keyboard.getFriendlyName());
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent keyEvent) {
				keyboard.handleKeyTyped(keyEvent);
			}
			
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				keyboard.handleKeyUp(keyEvent);				
			}
			
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				keyboard.handleKeyDown(keyEvent);
			}
		});
	 
		setVisible(true);
		
	}

}
