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
	private static final short MEM_MAP_SCREEN = 0;
	private static final short MEM_MAP_FONT = 1;
	private static final short MEM_MAP_PALETTE = 2;
	private static final short SET_BORDER_COLOR = 3;
	private static final short MEM_DUMP_FONT = 4;
	private static final short MEM_DUMP_PALETTE = 5;
	
	private static final int X_RESOLUTION = 128;
	private static final int Y_RESOLUTION = 96;
			
	
	public static final short[] builtInFont = new short[] {
		
		(short) 0xb79e, (short) 0x388e, (short) 0x722c, (short) 0x75f4, (short) 0x19bb, (short) 0x7f8f, (short) 0x85f9, (short) 0xb158, 
		(short) 0x242e, (short) 0x2400, (short) 0x082a, (short) 0x0800, (short) 0x0008, (short) 0x0000, (short) 0x0808, (short) 0x0808, 
		(short) 0x00ff, (short) 0x0000, (short) 0x00f8, (short) 0x0808, (short) 0x08f8, (short) 0x0000, (short) 0x080f, (short) 0x0000, 
		(short) 0x000f, (short) 0x0808, (short) 0x00ff, (short) 0x0808, (short) 0x08f8, (short) 0x0808, (short) 0x08ff, (short) 0x0000, 
		(short) 0x080f, (short) 0x0808, (short) 0x08ff, (short) 0x0808, (short) 0x6633, (short) 0x99cc, (short) 0x9933, (short) 0x66cc, 
		(short) 0xfef8, (short) 0xe080, (short) 0x7f1f, (short) 0x0701, (short) 0x0107, (short) 0x1f7f, (short) 0x80e0, (short) 0xf8fe, 
		(short) 0x5500, (short) 0xaa00, (short) 0x55aa, (short) 0x55aa, (short) 0xffaa, (short) 0xff55, (short) 0x0f0f, (short) 0x0f0f, 
		(short) 0xf0f0, (short) 0xf0f0, (short) 0x0000, (short) 0xffff, (short) 0xffff, (short) 0x0000, (short) 0xffff, (short) 0xffff, 
		(short) 0x0000, (short) 0x0000, (short) 0x005f, (short) 0x0000, (short) 0x0300, (short) 0x0300, (short) 0x3e14, (short) 0x3e00, 
		(short) 0x266b, (short) 0x3200, (short) 0x611c, (short) 0x4300, (short) 0x3629, (short) 0x7650, (short) 0x0002, (short) 0x0100, 
		(short) 0x1c22, (short) 0x4100, (short) 0x4122, (short) 0x1c00, (short) 0x1408, (short) 0x1400, (short) 0x081c, (short) 0x0800, 
		(short) 0x4020, (short) 0x0000, (short) 0x0808, (short) 0x0800, (short) 0x0040, (short) 0x0000, (short) 0x601c, (short) 0x0300, 
		(short) 0x3e49, (short) 0x3e00, (short) 0x427f, (short) 0x4000, (short) 0x6259, (short) 0x4600, (short) 0x2249, (short) 0x3600, 
		(short) 0x0f08, (short) 0x7f00, (short) 0x2745, (short) 0x3900, (short) 0x3e49, (short) 0x3200, (short) 0x6119, (short) 0x0700, 
		(short) 0x3649, (short) 0x3600, (short) 0x2649, (short) 0x3e00, (short) 0x0024, (short) 0x0000, (short) 0x4024, (short) 0x0000, 
		(short) 0x0814, (short) 0x2200, (short) 0x1414, (short) 0x1400, (short) 0x2214, (short) 0x0800, (short) 0x0259, (short) 0x0600, 
		(short) 0x3e59, (short) 0x5e00, (short) 0x7e09, (short) 0x7e00, (short) 0x7f49, (short) 0x3600, (short) 0x3e41, (short) 0x2200, 
		(short) 0x7f41, (short) 0x3e00, (short) 0x7f49, (short) 0x4100, (short) 0x7f09, (short) 0x0100, (short) 0x3e41, (short) 0x7a00, 
		(short) 0x7f08, (short) 0x7f00, (short) 0x417f, (short) 0x4100, (short) 0x2040, (short) 0x3f00, (short) 0x7f08, (short) 0x7700,
		(short) 0x7f40, (short) 0x4000, (short) 0x7f06, (short) 0x7f00, (short) 0x7f01, (short) 0x7e00, (short) 0x3e41, (short) 0x3e00, 
		(short) 0x7f09, (short) 0x0600, (short) 0x3e61, (short) 0x7e00, (short) 0x7f09, (short) 0x7600, (short) 0x2649, (short) 0x3200, 
		(short) 0x017f, (short) 0x0100, (short) 0x3f40, (short) 0x7f00, (short) 0x1f60, (short) 0x1f00, (short) 0x7f30, (short) 0x7f00, 
		(short) 0x7708, (short) 0x7700, (short) 0x0778, (short) 0x0700, (short) 0x7149, (short) 0x4700, (short) 0x007f, (short) 0x4100, 
		(short) 0x031c, (short) 0x6000, (short) 0x417f, (short) 0x0000, (short) 0x0201, (short) 0x0200, (short) 0x8080, (short) 0x8000, 
		(short) 0x0001, (short) 0x0200, (short) 0x2454, (short) 0x7800, (short) 0x7f44, (short) 0x3800, (short) 0x3844, (short) 0x2800, 
		(short) 0x3844, (short) 0x7f00, (short) 0x3854, (short) 0x5800, (short) 0x087e, (short) 0x0900, (short) 0x4854, (short) 0x3c00, 
		(short) 0x7f04, (short) 0x7800, (short) 0x047d, (short) 0x0000, (short) 0x2040, (short) 0x3d00, (short) 0x7f10, (short) 0x6c00, 
		(short) 0x017f, (short) 0x0000, (short) 0x7c18, (short) 0x7c00, (short) 0x7c04, (short) 0x7800, (short) 0x3844, (short) 0x3800, 
		(short) 0x7c14, (short) 0x0800, (short) 0x0814, (short) 0x7c00, (short) 0x7c04, (short) 0x0800, (short) 0x4854, (short) 0x2400, 
		(short) 0x043e, (short) 0x4400, (short) 0x3c40, (short) 0x7c00, (short) 0x1c60, (short) 0x1c00, (short) 0x7c30, (short) 0x7c00, 
		(short) 0x6c10, (short) 0x6c00, (short) 0x4c50, (short) 0x3c00, (short) 0x6454, (short) 0x4c00, (short) 0x0836, (short) 0x4100, 
		(short) 0x0077, (short) 0x0000, (short) 0x4136, (short) 0x0800, (short) 0x0201, (short) 0x0201, (short) 0x0205, (short) 0x0200
	};
			
	
	public static final short[] builtInPalette = new short[] {
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
	
	private short[] vram;
	

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
		short a = getProcessor().gpRegs[0];
		short b = getProcessor().gpRegs[1];
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
	public String getFriendlyName() {
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
		
		short[] fontRom;
		if (customFontMemStart == 0) fontRom = builtInFont;
		else fontRom = Arrays.copyOfRange(getProcessor().getMemory(), customFontMemStart, customFontMemStart + 256);
		
		int[] col = new int[4];
		col[0]  = fontRom[index]   >> 8;
		col[2]  = fontRom[index+1] >> 8;
		col[1]  = fontRom[index]   & 0xFF;
		col[3]  = fontRom[index+1] & 0xFF;
				
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
		
	private void bootScreen(final short b) {	
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
			getProcessor().performDump(b++, (short) builtInPalette[i]);
		}
	}

	private void dumpFont(short b) {
		for (int i=0; i < builtInPalette.length; i++) {
			getProcessor().performDump(b++, (short) builtInFont[i]);
		}		
	}
	
	private int[] getRgbFromPalette(int index) {
		short[] palette;
		if (customPaletteMemStart == 0) palette = builtInPalette;
		else palette = Arrays.copyOfRange(getProcessor().getMemory(),customPaletteMemStart, customPaletteMemStart + 16);
		int blue  = (0xF & palette[index]) << 4;
		int green = (0xF & (palette[index] >> 4)) << 4;
		int red   = (0xF & (palette[index] >> 8)) << 4;
		
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
