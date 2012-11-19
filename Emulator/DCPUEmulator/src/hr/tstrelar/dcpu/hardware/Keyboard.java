package hr.tstrelar.dcpu.hardware;

import java.awt.event.KeyEvent;

import hr.tstrelar.dcpu.Dcpu;

public abstract class Keyboard extends Device {
	
	public Keyboard(Dcpu dcpu) {
		super(dcpu);
	}
	
	public abstract void handleKeyDown(KeyEvent keyEvent);
	public abstract void handleKeyUp(KeyEvent keyEvent);
	public abstract void handleKeyTyped(KeyEvent keyEvent);

}
