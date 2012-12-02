package hr.tstrelar.dcpu.hardware;

import hr.tstrelar.dcpu.Dcpu;
import hr.tstrelar.dcpu.gui.MonitorWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;



public class LEM1802 extends Monitor {
	private static final int MEM_MAP_SCREEN = 0;
	private static final int MEM_MAP_FONT = 1;
	private static final int MEM_MAP_PALETTE = 2;
	private static final int SET_BORDER_COLOR = 3;
	private static final int MEM_DUMP_FONT = 4;
	private static final int MEM_DUMP_PALETTE = 5;
	
	private static final int X_RESOLUTION = 128;
	private static final int Y_RESOLUTION = 96;
			
	
	public static final int[] builtInFont = new int[] {
		
		 0xb79e,  0x388e,  0x722c,  0x75f4,  0x19bb,  0x7f8f,  0x85f9,  0xb158, 
		 0x242e,  0x2400,  0x082a,  0x0800,  0x0008,  0x0000,  0x0808,  0x0808, 
		 0x00ff,  0x0000,  0x00f8,  0x0808,  0x08f8,  0x0000,  0x080f,  0x0000, 
		 0x000f,  0x0808,  0x00ff,  0x0808,  0x08f8,  0x0808,  0x08ff,  0x0000, 
		 0x080f,  0x0808,  0x08ff,  0x0808,  0x6633,  0x99cc,  0x9933,  0x66cc, 
		 0xfef8,  0xe080,  0x7f1f,  0x0701,  0x0107,  0x1f7f,  0x80e0,  0xf8fe, 
		 0x5500,  0xaa00,  0x55aa,  0x55aa,  0xffaa,  0xff55,  0x0f0f,  0x0f0f, 
		 0xf0f0,  0xf0f0,  0x0000,  0xffff,  0xffff,  0x0000,  0xffff,  0xffff, 
		 0x0000,  0x0000,  0x005f,  0x0000,  0x0300,  0x0300,  0x3e14,  0x3e00, 
		 0x266b,  0x3200,  0x611c,  0x4300,  0x3629,  0x7650,  0x0002,  0x0100, 
		 0x1c22,  0x4100,  0x4122,  0x1c00,  0x1408,  0x1400,  0x081c,  0x0800, 
		 0x4020,  0x0000,  0x0808,  0x0800,  0x0040,  0x0000,  0x601c,  0x0300, 
		 0x3e49,  0x3e00,  0x427f,  0x4000,  0x6259,  0x4600,  0x2249,  0x3600, 
		 0x0f08,  0x7f00,  0x2745,  0x3900,  0x3e49,  0x3200,  0x6119,  0x0700, 
		 0x3649,  0x3600,  0x2649,  0x3e00,  0x0024,  0x0000,  0x4024,  0x0000, 
		 0x0814,  0x2200,  0x1414,  0x1400,  0x2214,  0x0800,  0x0259,  0x0600, 
		 0x3e59,  0x5e00,  0x7e09,  0x7e00,  0x7f49,  0x3600,  0x3e41,  0x2200, 
		 0x7f41,  0x3e00,  0x7f49,  0x4100,  0x7f09,  0x0100,  0x3e41,  0x7a00, 
		 0x7f08,  0x7f00,  0x417f,  0x4100,  0x2040,  0x3f00,  0x7f08,  0x7700,
		 0x7f40,  0x4000,  0x7f06,  0x7f00,  0x7f01,  0x7e00,  0x3e41,  0x3e00, 
		 0x7f09,  0x0600,  0x3e61,  0x7e00,  0x7f09,  0x7600,  0x2649,  0x3200, 
		 0x017f,  0x0100,  0x3f40,  0x7f00,  0x1f60,  0x1f00,  0x7f30,  0x7f00, 
		 0x7708,  0x7700,  0x0778,  0x0700,  0x7149,  0x4700,  0x007f,  0x4100, 
		 0x031c,  0x6000,  0x417f,  0x0000,  0x0201,  0x0200,  0x8080,  0x8000, 
		 0x0001,  0x0200,  0x2454,  0x7800,  0x7f44,  0x3800,  0x3844,  0x2800, 
		 0x3844,  0x7f00,  0x3854,  0x5800,  0x087e,  0x0900,  0x4854,  0x3c00, 
		 0x7f04,  0x7800,  0x047d,  0x0000,  0x2040,  0x3d00,  0x7f10,  0x6c00, 
		 0x017f,  0x0000,  0x7c18,  0x7c00,  0x7c04,  0x7800,  0x3844,  0x3800, 
		 0x7c14,  0x0800,  0x0814,  0x7c00,  0x7c04,  0x0800,  0x4854,  0x2400, 
		 0x043e,  0x4400,  0x3c40,  0x7c00,  0x1c60,  0x1c00,  0x7c30,  0x7c00, 
		 0x6c10,  0x6c00,  0x4c50,  0x3c00,  0x6454,  0x4c00,  0x0836,  0x4100, 
		 0x0077,  0x0000,  0x4136,  0x0800,  0x0201,  0x0201,  0x0205,  0x0200
	};
			
	
	public static final int[] builtInPalette = new int[] {
							0x0000,	0x000a,	0x00a0,	0x00aa,
							0x0a00,	0x0a0a,	0x0a50,	0x0aaa,
							0x0555,	0x055f,	0x05f5,	0x05ff,
							0x0f55, 0x0f5f,	0x0ff5,	0x0fff			
	};
		
	private int memStart;
	private int customFontMemStart;
	private int customPaletteMemStart;
	
	private int borderColor = 11;
	
	private MonitorWindow window;
	
	private volatile boolean blinkOn;
	private Timer blinkTimer;
	
	private int[] vram;
	

	public LEM1802(Dcpu dcpu) {
		super(dcpu);
		vram = getProcessor().getMemory();
	}

	@Override
	public int getID() {
		return 0x7349f615;
	}

	@Override
	public int getManufacturer() {
		return 0x1c6c8b36;
	}

	@Override
	public int getVersion() {
		return 0x1802;
	}

	@Override
	public void interrupt() {
		int a = getProcessor().gpRegs[0];
		int b = getProcessor().gpRegs[1];
		switch(a) {
		case MEM_MAP_SCREEN:
			bootScreen(b);
			break;
			
		case MEM_MAP_FONT:
			customFontMemStart = (0xFFFF&b);
			break;
			
		case MEM_MAP_PALETTE:
			customPaletteMemStart = (0xFFFF&b);
			break;
			
		case SET_BORDER_COLOR:
			borderColor = b;
			break;
			
		case MEM_DUMP_FONT:
			dumpFont(b);
			break;
			
		case MEM_DUMP_PALETTE:
			dumpPalette(b);
			break;
		}
	}
	
	@Override
	public String toString() {
		return("LEM1802");
	}

	@Override
	public int getXSize() {
		return X_RESOLUTION;
	}

	@Override
	public int getYSize() {
		return Y_RESOLUTION;
	}
	
    @Override
	public BufferedImage getCell(int position) {
		int memValue = vram[position + memStart];
		int index = (0x7F & memValue) << 1;
		int[] background = getRgbFromPalette(0xF & (memValue >> 8));
		int[] foreground = getRgbFromPalette(0xF & (memValue >> 12));
		boolean isBlinking = ((memValue >> 7) & 1) == 1 ? true : false;
		
		int fontHi;
		int fontLo;
		if (customFontMemStart == 0) {
			fontHi = builtInFont[index];
			fontLo = builtInFont[index+1];
		}
		else {
			fontHi = getProcessor().getMemory()[customFontMemStart+index];
			fontLo = getProcessor().getMemory()[customFontMemStart+index+1];
		}
		
		int[] col = new int[4];
		col[0]  = fontHi >> 8;
		col[2]  = fontLo >> 8;
		col[1]  = fontHi & 0xFF;
		col[3]  = fontLo & 0xFF;
				
		BufferedImage ret = new BufferedImage(4, 8, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = ret.getRaster();
		boolean show = !isBlinking || (isBlinking && blinkOn);
		int k = 0;
		for (int j = 0; j < 8; j++) {
			for (int i = 0; i < 4; i++) {
				if ((((col[i] >> k) & 1)  == 1) && show) {
					raster.setPixel(i, j, foreground);
				} else {
					raster.setPixel(i, j, background);
				}
			}
			k++;
		}
		
		return ret;
	}
    
    @Override
	public Color getBorderColor() {
    	int[] colorValues = getRgbFromPalette(borderColor);
		return new Color(colorValues[0], colorValues[1], colorValues[2], 0xFF);
	}
		
	private void bootScreen(final int b) {	
		// set the video ram
		
		if (b != 0) {
			// screen was off
							
			if (memStart == 0) {
				blinkTimer = new Timer(true);
				blinkTimer.schedule(new Blinker(), 500, 500);
				System.out.println("Turning on the screen");
				System.out.println(memStart +  " B reg = 0x" + Integer.toHexString(0xFFFF&b));
				window = MonitorWindow.instance(this);
			}
			memStart = 0xFFFF & b;
							
		// disconnect screen
		} else {
			System.out.println("Monitor disconnecting");
			if (window != null) MonitorWindow.destroy();
			memStart = 0;
		}
		
		
	}
	
	private void dumpPalette(int b) {
		for (int i=0; i < builtInPalette.length; i++) {
			getProcessor().performDump(b++,  builtInPalette[i]);
		}
	}

	private void dumpFont(int b) {
		for (int i=0; i < builtInPalette.length; i++) {
			getProcessor().performDump(b++,  builtInFont[i]);
		}		
	}
	
	private int[] getRgbFromPalette(int index) {
		int color;
		if (customPaletteMemStart == 0) color = builtInPalette[index];
		else color = getProcessor().getMemory()[index+customPaletteMemStart];
		int blue  = (0xF & color) << 4;
		int green =  0xF0 & color;
		int red   = (0xF00 & color) >> 4;
		
		return new int[] {red, green, blue, 0xFF};
	}
		
	class Blinker extends TimerTask {
	
		@Override
		public void run() {
			if (blinkOn) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				blinkOn = false;
			}
			else blinkOn = true;
			
		}
	}

	

	

}
