package hr.tstrelar.dcpu.hardware;

import hr.tstrelar.dcpu.Dcpu;

import java.util.Timer;
import java.util.TimerTask;

public class GenericClock extends Device {
	private int ticks;
	private boolean interruptsOn;
	private short message;
	private Timer timer;
	public GenericClock(Dcpu dcpu) {
		super(dcpu);
	}

	@Override
	public int getID() {
		return 0x12d0b402;
	}

	@Override
	public int getManufacturer() {
		return 0;
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void interrupt() {
		int delay = 0;
		switch (getProcessor().gpRegs[0]) {
		case 0:
			ticks = 0;
			if (getProcessor().gpRegs[1] != 0) {
				
				delay = 1000 / (60 / getProcessor().gpRegs[1]);
				if (timer != null) {
					timer.cancel();
				}
				timer = new Timer(true);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (interruptsOn) getProcessor().handleInterrupt(message);
						ticks++;
					}
				}, delay, delay);
			} else {
				timer.cancel();
			}
			break;
		case 1:
			getProcessor().gpRegs[2] = (short) ticks;
			break;
		case 2:
			message = getProcessor().gpRegs[1];
			if (message != 0) {
				interruptsOn = true;
			} else interruptsOn = false;
		}
	}

}
