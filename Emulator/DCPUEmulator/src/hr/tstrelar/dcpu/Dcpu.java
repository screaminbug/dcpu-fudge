package hr.tstrelar.dcpu;

import hr.tstrelar.dcpu.hardware.Device;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

import org.omg.CORBA.UShortSeqHelper;
import org.omg.CORBA.UShortSeqHolder;


public class Dcpu {
	public static final int USHORT_MASK 	  = 0xFFFF; 

	public static final int GP_REGISTER_COUNT  = 0x08; // we currently have 8 general purpose registers -- A, B, C, X, Y, Z, I, J
	public static final int MEMORY_CAPACITY    = 0x10000; // 65536 words

	public static final int REG_BOUND 	    = GP_REGISTER_COUNT;
	public static final int DREG_BOUND 	    = GP_REGISTER_COUNT << 1;
	public static final int GP_REG_BOUND    = DREG_BOUND + GP_REGISTER_COUNT;
	public static final int PUPO_VAL        = GP_REG_BOUND;
	public static final int PEEK_VAL        = PUPO_VAL   + 1;
	public static final int PICK_VAL        = PEEK_VAL   + 1;
	public static final int SP_VAL          = PICK_VAL   + 1;
	public static final int PC_VAL          = SP_VAL     + 1;
	public static final int EX_VAL          = PC_VAL     + 1;
	public static final int NEXTW_VAL       = EX_VAL     + 1;
	public static final int LNEXTW_VAL      = NEXTW_VAL  + 1;
	public static final int LITERAL_BOUND   = LNEXTW_VAL + 1;
	public static final int UPPER_BOUND 	= LITERAL_BOUND + 0x20;

	private int[] memory = new int[MEMORY_CAPACITY];
	public int[] gpRegs = new int[GP_REGISTER_COUNT]; // A, B, C, X, Y, Z, I, J, in that order
	private int sp, pc, ex, ia;
	private long cycle;
	private boolean isSkipping;
	private long cyclesPerSecond = 100000;
	private StringBuilder log = new StringBuilder(50000);

	private int tempPointer;
	private ModificationType modify;

	private List<Device> devices = new ArrayList<>();

	private boolean queueInterrupts;
	private Queue<Integer> interruptQueue = new ArrayDeque<>();
	private boolean caughtFire;
	private Random random = new Random(System.currentTimeMillis());
		
    private Boolean isInterrupted = new Boolean(false);

	public Dcpu(int[] memory) {
		this.memory = memory;
	}
	
	public void connectDevice(Class<? extends Device> deviceClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		devices.add(deviceClass.getConstructor(this.getClass()).newInstance(this));
	}
	
	public Dcpu(int[] memory, long speed) {
		this.memory = memory;
		cyclesPerSecond = speed;
	}

	public void run() throws InterruptedException {
		
		while (true) {
			long startTime = System.nanoTime();
			long cyclesBefore = cycle;
			long instructionsToRun = 1024;
			while ((instructionsToRun--) >= 0) {
//				System.out.printf("Instruction: 0x%s\n", 
//						Integer.toHexString(USHORT_MASK & memory[USHORT_MASK & pc]));
			    

				decodeInstruction(memory[USHORT_MASK & pc++]);
				if (!isSkipping && isInterrupted) {
					executeInterrupt();
				}

//				System.out.printf("A = 0x%s\nB = 0x%s\nC = 0x%s\nX = 0x%s\nY = 0x%s\nZ = 0x%s\nI = 0x%s\nJ = 0x%s\n", 
//						Integer.toHexString(USHORT_MASK & gpRegs[0]), 
//								Integer.toHexString(USHORT_MASK & gpRegs[1]),
//										Integer.toHexString(USHORT_MASK & gpRegs[2]), 
//												Integer.toHexString(USHORT_MASK & gpRegs[3]), 
//														Integer.toHexString(USHORT_MASK & gpRegs[4]), 
//																Integer.toHexString(USHORT_MASK & gpRegs[5]), 
//																		Integer.toHexString(USHORT_MASK & gpRegs[6]), 
//																				Integer.toHexString(USHORT_MASK & gpRegs[7]));
//				System.out.printf("SP = %d, PC = %d, EX = %d, IA = %d\n\n", sp, pc, ex, ia);
	//			
			}
			long timeTook = System.nanoTime() - startTime;
			int cycles = (int) (cycle - cyclesBefore);
			long shouldTook = (1000000000L * cycles / cyclesPerSecond);
			long adjustmentNano = (shouldTook - timeTook);
			long adjustmentMili = 0;
			if (adjustmentNano > 999999) {
				adjustmentMili = adjustmentNano / 1000000L;
				adjustmentNano = 0;
			}
//			log.append("\n\n\nThe average frequency is: " + cycles * 1000000000L / timeTook);
//			log.append("\nTime took: " + timeTook);
//			log.append("\nAdjustment: " + adjustmentMili + " ms" + " and " + adjustmentNano + " ns");
//			log.append("\nCycles: " + cycles);
//			log.append("\nShould took: " + shouldTook);
			
			
			if (adjustmentMili > 0 || adjustmentNano > 0) Thread.sleep(adjustmentMili, (int) adjustmentNano);
//			log.append("\nAfter adjustment took: " + (System.nanoTime() - startTime));
//			log.append("\nThe average frequency after adjustment is: " + cycles * 1000000000L / (double)(System.nanoTime() - startTime));
//			
//			System.out.println(log);
		}

	}
	
	public void performDump(int pointer, int data) {
		memory[USHORT_MASK & pointer] = data;
		cycle++;
	}
	
	

	public synchronized void handleInterrupt(Integer message) {
		if (message != null) {		
			// Queue interrupt
			if (queueInterrupts) {
				interruptQueue.add(message);
				if (interruptQueue.size() > 0xFF) {
					caughtFire = true;
				}
			// Trigger interrupt
			} else if (ia != 0) {
				interruptQueue.add(message);
				isInterrupted = true;
				queueInterrupts = true;
			}
		}

	}

	private void executeInterrupt() {
		memory[USHORT_MASK & --sp] = pc; // SET PUSH PC
		cycle++;
		memory[USHORT_MASK & --sp] = gpRegs[0];
		cycle++;
		pc = ia;
		cycle++;
		gpRegs[0] = interruptQueue.poll();
		cycle++;
		isInterrupted = false;
	}

	private void setHardwareInfo(int deviceNr) {
		if ((USHORT_MASK & deviceNr) >= devices.size()) {
			handleError("Attempted to query a non-existing device");
			return;
		}
		
		Device dev = devices.get(USHORT_MASK & deviceNr);
		int hwID = dev.getID();
		int man = dev.getManufacturer();
		gpRegs[0] = (int) hwID;
		gpRegs[1] = (int) (hwID >> 16);
		gpRegs[2] = (int) dev.getVersion();
		gpRegs[3] = (int) man;
		gpRegs[4] = (int) (man >> 16);

	}

	/**
	 * Decodes the a and b values in the instruction and returns decoded values which can be one word long
	 * 
	 * @param value 5-bit or 6-bit value extracted from the instruction
	 * @param isValueA Must be true if decoding "a" value, otherwise false
	 * @return Decoded value
	 */

	private int decodeValue(byte value, boolean isValueA) {
		int retVal = 0;

		// it's safe to use byte, as arg should never be wider than 6 bits
		// if the signature bit is turned on, something went terribly wrong
		assert(value >= 0) : "Value decoder encountered abnormal value";

		if (value < LITERAL_BOUND) {		
			int pointer = -1;

			// register
			if (value < REG_BOUND) {
				modify = ModificationType.REGISTER;
				pointer = value;
				retVal = gpRegs[pointer];

				// [register]
			} else if (value < DREG_BOUND) {
				pointer = USHORT_MASK & gpRegs[value - 0x08];
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];

				// [register + next word]
			} else if (value < GP_REG_BOUND) {
				pointer = USHORT_MASK & (gpRegs[value - 0x10] + memory[USHORT_MASK & pc++]);
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];
				if (!isSkipping) cycle++;

				// POP / PUSH
				// NOTE: if skipping we must not modify SP
			} else if (!isSkipping && value == PUPO_VAL) {
				// POP / [SP++]   	
				if (isValueA) {
					modify = ModificationType.EMULATOR_ERROR; // POP can never be the target (unless if IAG POP and RFI POP, but assemblers should dissalow this)
					retVal = memory[USHORT_MASK & sp++];
				}
				// PUSH / [--SP]
				else {
					pointer = USHORT_MASK & --sp;
					modify = ModificationType.MEMORY;
					retVal = 0; 
				}


				// [SP] / PEEK
			} else if (value == PEEK_VAL) {
				pointer = USHORT_MASK & sp;
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];

				// [SP + next word] / PICK n
			} else if (value == PICK_VAL) {
				pointer = USHORT_MASK & (sp + pc++);
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];
				if (!isSkipping) cycle++;

				// SP
			} else if (value == SP_VAL) {
				modify = ModificationType.SP;
				retVal = sp;

				// PC
			} else if (value == PC_VAL) {
				modify = ModificationType.PC;
				retVal = pc;

				// EX
			} else if (value == EX_VAL) {
				modify = ModificationType.EX;
				retVal = ex;

				// [next word]
			} else if (value == NEXTW_VAL) {
				pointer = USHORT_MASK & memory[USHORT_MASK & pc++];
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];
				if (!isSkipping) cycle++;

				// next word literal
			} else if (value == LNEXTW_VAL) {
				pointer = USHORT_MASK & pc++;
				modify = ModificationType.MEMORY;
				retVal = memory[pointer];
				if (!isSkipping) cycle++;

			}	
			tempPointer = pointer;

		} else if (value < UPPER_BOUND){
			modify = ModificationType.NONE;  // writing to a literal must fail silently
			retVal = (int)(value - 0x21);  // literal value stored as signed int from -1 to 30
		}


		return retVal;

	}


	public void decodeInstruction(int word) {
		byte opcode = (byte) (0b011111 &  word);
		byte bi     = (byte) (0b011111 & (word >>  5));
		byte ai     = (byte) (0b111111 & (word >> 10));
			
		if (caughtFire) {
			System.out.println("I'm on fire!!!");
			int randomValue = (int) random.nextInt(0xFFFF);
			switch (random.nextInt(5)) {
				case 0: 
					gpRegs[random.nextInt(7)] = randomValue;
					break;
				
				case 1:
					sp = randomValue;
					break;
				
				case 2:
					pc = randomValue;
					break;
					
				case 3:
					ex = randomValue;
					break;
					
				case 4:
					ia = randomValue;
					break;
					
				case 5:
					memory[random.nextInt(0xFFFF)] = randomValue;
					break;
			}
		}

		switch(opcode) {
		case 0x00:
			switch(bi) {
			case 0x00:notImpl(); break; 
			case 0x01:jsrOp(ai); break; 
			case 0x02:notImpl(); break;
			case 0x03:notImpl(); break;
			case 0x04:notImpl(); break; 
			case 0x05:notImpl(); break;
			case 0x06:notImpl(); break; 
			case 0x07:notImpl(); break; 
			case 0x08:intOp(ai); break; 
			case 0x09:iagOp(ai); break; 
			case 0x0a:iasOp(ai); break; 
			case 0x0b:rfiOp(ai); break;
			case 0x0c:iaqOp(ai); break; 
			case 0x0d:notImpl(); break;
			case 0x0e:notImpl(); break;	
			case 0x0f:notImpl(); break;
			case 0x10:hwnOp(ai); break; 
			case 0x11:hwqOp(ai); break;
			case 0x12:hwiOp(ai); break; 
			case 0x13:notImpl(); break;
			case 0x14:notImpl(); break;	
			case 0x15:notImpl(); break; 
			case 0x16:notImpl(); break; 
			case 0x17:notImpl(); break;	
			case 0x18:notImpl(); break; 
			case 0x19:notImpl(); break; 
			case 0x1a:notImpl(); break;	
			case 0x1b:notImpl(); break; 
			case 0x1c:notImpl(); break; 
			case 0x1d:notImpl(); break;
			case 0x1e:notImpl(); break; 
			case 0x1f:notImpl(); break;			
			}
			break;
		case 0x01:setOp(bi, ai); break;	
		case 0x02:addOp(bi, ai); break;	
		case 0x03:subOp(bi, ai); break;	
		case 0x04:mulOp(bi, ai); break;	
		case 0x05:mliOp(bi, ai); break;	
		case 0x06:divOp(bi, ai); break; 
		case 0x07:dviOp(bi, ai); break;	
		case 0x08:modOp(bi, ai); break;	
		case 0x09:mdiOp(bi, ai); break;	
		case 0x0a:andOp(bi, ai); break;	
		case 0x0b:borOp(bi, ai); break;	
		case 0x0c:xorOp(bi, ai); break;	
		case 0x0d:shrOp(bi, ai); break;	
		case 0x0e:asrOp(bi, ai); break;	
		case 0x0f:shlOp(bi, ai); break;	
		case 0x10:ifbOp(bi, ai); break;	
		case 0x11:ifcOp(bi, ai); break;	
		case 0x12:ifeOp(bi, ai); break;
		case 0x13:ifnOp(bi, ai); break;	
		case 0x14:ifgOp(bi, ai); break;	
		case 0x15:ifaOp(bi, ai); break;	
		case 0x16:iflOp(bi, ai); break;	
		case 0x17:ifuOp(bi, ai); break;	
		case 0x18:notImplBas();  break;
		case 0x19:notImplBas();  break;
		case 0x1a:adxOp(bi, ai); break;	
		case 0x1b:sbxOp(bi, ai); break;	
		case 0x1c:notImplBas();  break;
		case 0x1d:notImplBas();  break;	
		case 0x1e:stiOp(bi, ai); break;
		case 0x1f:stdOp(bi, ai); break;
		}
		
	}

	private void notImplBas() {
		handleError("Encountered unknown basic opcode.");

	}

	private void notImpl() {
		handleError("Encountered unknown special opcode.");

	}
	
	private void handleError(String message) {
		pc = sp = ia = ex = 0;
		System.out.println();
		System.out.println("************************************************");
		System.out.println(message);
		System.out.println("RESETING REGISTERS!!!");
		if (caughtFire) {
			System.out.println("!!!Putting out fire!!! (with gasoline)");
			int interrupt = interruptQueue.poll();
			interruptQueue.clear();
			interruptQueue.add(interrupt);			
			caughtFire = false;
		}
		System.out.println("************************************************");
		System.out.println();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void hwiOp(byte ai) {
		int device = decodeValue(ai, true);
		if (!isSkipping) {
			if ((USHORT_MASK & device) >= devices.size()) {
				handleError("Attempted to send a hardware interrupt to a non-existing device.");
				
				System.exit(-1);//return;
			}
			cycle += 4;
			devices.get(device).interrupt();		
			
		} else cycle++;
		
	}

	private void hwnOp(byte ai) {
		decodeValue(ai, true);
		if (!isSkipping) {
			doWrite((int)devices.size()); 
			cycle += 2;
		} else cycle++;

	}

	private void hwqOp(byte ai) {
		int deviceNumber = decodeValue(ai, true);
		if (!isSkipping) {
			setHardwareInfo(deviceNumber); 
			cycle += 4;
		} else cycle++;

	}

	private void iaqOp(byte ai) {
		int source = decodeValue(ai, true);
		if (!isSkipping) {
			if (source != 0) {
				queueInterrupts = true;
			} else {
				queueInterrupts = false;
			}
			cycle +=2;
		} else {
			isSkipping = false;
			cycle++;
		}

	}

	private void iasOp(byte ai) {
		int source = decodeValue(ai, true);
		if (!isSkipping) {
			ia = source;
		}
		isSkipping = false;
		cycle++;

	}

	private void iagOp(byte ai) {
		decodeValue(ai, false);
		if (doWrite(ia)) cycle++;

	}

	private void rfiOp(byte ai) {
		decodeValue(ai, true);
		if (!isSkipping) {
			queueInterrupts = false;
			gpRegs[0] = memory[USHORT_MASK & sp++];
			pc = memory[USHORT_MASK & sp++];
			cycle += 3;
			
		} else cycle++;
		isSkipping = false;
	}

	private void intOp(byte ai) {
		int source = decodeValue(ai, true);
		if (!isSkipping) {
			handleInterrupt(source);		
			// cycles added in triggerInterrupt method
		} else {
			isSkipping = false;
		}
		
	}

	private void jsrOp(byte ai) {
		int retVal = decodeValue(ai, true);
		if (!isSkipping) {
			memory[USHORT_MASK & --sp] = pc;
			pc = retVal;
			cycle += 3;
		} else {
			isSkipping = false;
			cycle++;
		}
		
	}

	private void stdOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = source;
		if (doWrite(target)) {
			--gpRegs[6];
			--gpRegs[7];
			cycle += 2;
		}
		
	}

	private void stiOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = source;
		if (doWrite(target)) {
			++gpRegs[6];
			++gpRegs[7];
			cycle += 2;
		}
		
	}

	private void sbxOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = (USHORT_MASK & target) - (USHORT_MASK & source) + (USHORT_MASK & ex);

		if (doWrite((int) (USHORT_MASK & target))) {
			if (target < 0) {
				ex = (int) USHORT_MASK;
			} else if (target > 0xFFFF) {
				ex = 1;
			} else {
				ex = 0;
			}
			cycle += 3;
		}
		

	}

	private void adxOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = (USHORT_MASK & target) + (USHORT_MASK & source) + (USHORT_MASK & ex);

		if (doWrite(USHORT_MASK & target)) {
			if (target > USHORT_MASK) {
				ex = 1;
			} else ex = 0;
			cycle += 3;
		}
}

	private void ifuOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!(target < source)) {
				isSkipping = true;
			}
		} else cycle++;

	}

	private void iflOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!((USHORT_MASK & target) < (USHORT_MASK & source))) {
				isSkipping = true;
			}
		} else cycle++;

	}

	private void ifaOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!(target > source)) {
				isSkipping = true;
			}
		} else cycle++;
	}

	private void ifgOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!((USHORT_MASK & target) > (USHORT_MASK & source))) {
				isSkipping = true;
			}
		} else cycle++;

	}

	private void ifnOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!(target != source)) {
				isSkipping = true;
			}
		} else cycle++;
	}

	private void ifeOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!(target == source)) {
				isSkipping = true;
			}
		} else cycle++;
	}

	private void ifcOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!((target & source) == 0)) {
				isSkipping = true;
			}
		} else cycle++;

	}

	private void ifbOp(byte bi, byte ai) {
		int source = USHORT_MASK & decodeValue(ai, true);
		int target = USHORT_MASK & decodeValue(bi, false);

		if (!isSkipping) {
			cycle += 2;
			if (!((target & source) != 0)) {
				isSkipping = true;
			}
		} else cycle++;

	}

	private void shlOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		int tg = target << source;
		if (doWrite(USHORT_MASK & tg)) {
			ex = ((target << source) >> 16) & USHORT_MASK;
			cycle++;
		}
	}

	private void asrOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		short tg = (short) ((USHORT_MASK & target) >> source);
		if (doWrite(tg)) {
			ex = ((target << 16) >>> source) & USHORT_MASK;
			cycle++;
		}  

	}

	private void shrOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		int tg = target >>> source;
		if (doWrite(USHORT_MASK & tg)) {
			ex = ((target << 16) >> source) & USHORT_MASK;
			cycle++;
		}  
	}

	private void xorOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = target ^ source;
		if (doWrite(USHORT_MASK & target)) cycle++;
	}

	private void borOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		target = target | source;
		if (doWrite(USHORT_MASK & target)) cycle++;
	}

	private void andOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target = target & source;
		if (doWrite(USHORT_MASK & target)) cycle++;
	}

	private void mdiOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		if (source != 0 ) {
			target = (short) (target % source);
		} else {
			target = 0;
		}
		if (doWrite(USHORT_MASK & target)) cycle += 3;
	}

	private void modOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		if (source != 0 ) {
			target = (USHORT_MASK & target) % (USHORT_MASK & source);
		} else {
			target = 0;
		}
		
		if (doWrite(USHORT_MASK & target)) cycle += 3;
	}

	private void dviOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		if (target != 0) {  
			short tg = (short) (target / source);
			if (doWrite(USHORT_MASK & tg)) {
				ex = ((target << 16) / source) & USHORT_MASK;
				cycle += 3;
			} 

		} else {
			if (doWrite(0)) {
				ex = 0;
				cycle += 3;
			} 
		} 
	}

	private void divOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		if (source != 0) {  
			int tg = (USHORT_MASK & target) / (USHORT_MASK & source);
			if (doWrite(USHORT_MASK & tg)) {
				ex = ((target << 16) / source) & USHORT_MASK;
				cycle += 3;
			} 

		} else {
			if (doWrite(0)) {
				ex = 0;
				cycle += 3;
			} 
		} 
	}

	private void mliOp(byte bi, byte ai) {
		short source = (short) decodeValue(ai, true);
		short target = (short) decodeValue(bi, false);

		short tg = (short) (target * source);
		if (doWrite(USHORT_MASK & tg)) {
			ex = ((target * source) >> 16) & USHORT_MASK; 
			cycle += 2;
		}  

	}

	private void mulOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		int tg = (USHORT_MASK & target) * (USHORT_MASK & source);
		if (doWrite(tg)) {
			ex = ((target * source) >> 16) & USHORT_MASK; 
		    cycle += 2;
		}  


	}

	private void subOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target -= USHORT_MASK & source;
		if (doWrite((int) (USHORT_MASK & target))) {
			if (target < 0) ex = -1; 
			else ex = 0;
			cycle += 2;
		}  

	}

	private void addOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);

		target += USHORT_MASK & source;
		if (doWrite((USHORT_MASK & target))) {
			if (target > USHORT_MASK) ex = 1; 
			else ex = 0;
			cycle += 2;
		} 

	}

	private void setOp(byte bi, byte ai) {
		int source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
        		
		target = USHORT_MASK & source;
		if (doWrite(target)) cycle++;
	}


	private boolean doWrite(int value) {
		boolean noSkip = !this.isSkipping;
		if (noSkip) {
			switch(modify) {
			case MEMORY:
				memory[tempPointer] = value;
				break;
			case REGISTER:
				gpRegs[tempPointer] = value;
				break;
			case PC:
				pc = value;
				break;
			case SP:
				sp = value;
				break;
			case EX:
				ex = value;
				break;
			case NONE:
				break; // in case we try to write to a literal we fail silently
			case EMULATOR_ERROR:
				throw new RuntimeException("Invalid instruction at PC = " + Integer.toHexString(USHORT_MASK & --pc) + ", " + Integer.toHexString(USHORT_MASK & memory[USHORT_MASK & pc]));
			default:
				assert(false) : "Check Modification Type enum";
			}
		} else cycle++; // just one cycle cost if skipping
		this.isSkipping = false;
		return noSkip;
	}

	public int[] getMemory() {
		// TODO Auto-generated method stub
		return memory;
	}



}
