package hr.tstrelar.dcpu.hardware;

import hr.tstrelar.dcpu.Dcpu;

public abstract class Device {	

	Dcpu connectedDcpu;
	
	protected Device(Dcpu dcpu) {
		connectedDcpu = dcpu;
	}
	
	public Dcpu getProcessor() {
		return connectedDcpu; 
	}
	
	public abstract int getID();
	public abstract int getManufacturer();
	public abstract int getVersion();
	public abstract void interrupt();
	public abstract String getFriendlyName();
}
