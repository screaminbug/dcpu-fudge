package hr.tstrelar.dcpu.hardware;

import java.awt.image.BufferedImage;

import hr.tstrelar.dcpu.Dcpu;

public abstract class Monitor extends Device {

	protected Monitor(Dcpu dcpu) {
		super(dcpu);
	}
	
	public abstract int getXSize();
	public abstract int getYSize();
	public abstract BufferedImage[] getScreen();

}
