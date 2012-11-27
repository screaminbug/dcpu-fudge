package hr.tstrelar.dcpu.hardware;

import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Queue;

import hr.tstrelar.dcpu.Dcpu;
import hr.tstrelar.dcpu.gui.KeyboardWindow;

public class GenericKeyboard extends Keyboard {
	
	private Queue<Short> buffer = new ArrayDeque<>();
	private Integer intMessage;
	private boolean repeatedKey;
	
	public GenericKeyboard(Dcpu dcpu) {
		super(dcpu);
		KeyboardWindow.instance(this);
	}

	@Override
	public int getID() {
		return 0x30cf7406;
	}

	@Override
	public int getManufacturer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void interrupt() {
		Short key = buffer.poll();
		switch(getProcessor().gpRegs[0]) {
		case 0:
			buffer.clear();
			break;
		case 1:
			getProcessor().gpRegs[2] = key != null ? key : 0;
			break;
		case 2:
			if (key != null && getProcessor().gpRegs[2] == key) {
				getProcessor().gpRegs[2] = 1;
			} else {
				getProcessor().gpRegs[2] = 0;
			}
			break;
		case 3:
			if (getProcessor().gpRegs[1] != 0) {
				intMessage = getProcessor().gpRegs[1];
			} else intMessage = null;
			break;
			
		}

	}
	
	private void handleKey(Short keyCode) {
		if (keyCode != null) {
			if (intMessage != null) {
				getProcessor().handleInterrupt(intMessage);
			} else {
				buffer.add(keyCode);
			}		
		}
		System.out.println(keyCode);
		
	}

	@Override
	public void handleKeyDown(KeyEvent keyEvent) {
		//if (!repeatedKey) {// && keyEvent.getKeyCode() != KeyEvent.VK_UNDEFINED) {
			handleKey(decodeKeyCode(keyEvent));
			repeatedKey = true;
		//}
				
	}
	@Override
	public void handleKeyUp(KeyEvent keyEvent) {
		//if (repeatedKey) {
			//handleKey(decodeKeyCode(keyEvent));
			repeatedKey = false;
		//}
		
	}

	@Override
	public void handleKeyTyped(KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.VK_UNDEFINED) {
			handleKey(decodeKeyCode(keyEvent));	
		}
	}
	
	private Short decodeKeyCode(KeyEvent keyEvent) {
		Short retVal = null;
		switch(keyEvent.getKeyCode()) {
		case KeyEvent.VK_BACK_SPACE:
			retVal = 0x10;
			break;
			
		case KeyEvent.VK_ENTER:
			retVal = 0x11;
			break;
			
		case KeyEvent.VK_INSERT:
			retVal = 0x12;
		    break;
			
		case KeyEvent.VK_DELETE:
			retVal = 0x13;
			break;
			
		case KeyEvent.VK_UP:
			retVal = 0x80;
			break;
			
		case KeyEvent.VK_DOWN:
			retVal = 0x81;
			break;
			
		case KeyEvent.VK_LEFT:
			retVal = 0x82;
			break;
			
		case KeyEvent.VK_RIGHT:
			retVal = 0x83;
			break;
			
		case KeyEvent.VK_SHIFT:
			retVal = 0x90;
			break;
			
		case KeyEvent.VK_CONTROL:
			retVal = 0x91;
			break;
			
		case KeyEvent.VK_UNDEFINED:
			int keyCode = keyEvent.getKeyChar();
			if (keyCode >= 0x20 && keyCode <= 0x7F) {
				retVal = (short) keyCode;
			} else retVal = null;
			break;
		}
		
		return retVal;
	}


	@Override
	public String getFriendlyName() {
		return("Generic Keyboard");
	}

}
