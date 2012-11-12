package hr.tstrelar.dcpu;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
	
	public static void main(String[] args) {
		if (args.length == 0) returnError();
		
		short[] program = new short[0x10000];
		int byteRead = 0;
		boolean isUpper = true;
		int i = 0;
		
		try {
			FileInputStream fis = new FileInputStream(args[0]);
			try {
				byteRead = fis.read();
				while (byteRead != -1) {
					if (isUpper) {
						program[i] = (short) (byteRead << 8);
						isUpper = false;
					} else {
						program[i++] |= (short) byteRead;
						isUpper = true;
					}
					byteRead = fis.read();
				}
			} finally {
				fis.close();
			}	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("No such file");
			System.exit(-2);
		} catch (IOException ioe) {
			System.out.println("There was a problem with file IO.");
			System.exit(-3);
		}
		
		Dcpu dcpu = new Dcpu(program);
		dcpu.run();
		
	}
	
	private static void returnError() {
		System.out.println("Binary filename not specified. Exiting...");
		System.exit(-1);
	}
}
