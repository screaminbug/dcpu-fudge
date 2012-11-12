package hr.tstrelar.dcpu;


public class Dcpu {
	public static final int USHORT_MASK 	  = 0xFFFF; // bitmask for artificially creating unsigned shorts (converting signed shorts to signed ints)
	                                            // this must be done every time we reference the memory (dereferencing pointer) and for every unsigned operation
	
	public static final int GP_REGISTER_COUNT  = 0x08; // we currently have 8 general purpose registers -- A, B, C, X, Y, Z, I, J
	public static final int MEMORY_CAPACITY    = 0x10000; // 65536 words
	
	public static final int REG_BOUND 	    = GP_REGISTER_COUNT;
	public static final int DREG_BOUND 	    = GP_REGISTER_COUNT << 1;
	public static final int GP_REG_BOUND    = DREG_BOUND + 8;
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
	
	private short[] memory = new short[MEMORY_CAPACITY];
	private short[] gpRegs = new short[GP_REGISTER_COUNT]; // A, B, C, X, Y, Z, I, J, in that order
	private short sp, pc, ex, ia;
	private long cycle;
	
	private int tempPointer;
	private ModificationType modify;
	
	public Dcpu(short[] memory) {
		this.memory = memory;
	}
	
	public void run() {
		while (true) {
			decodeInstruction(memory[pc++]);
			cycle++;
		}
		
	}
	
	/**
	 * Decodes the a and b values in the instruction and returns decoded values which can be one word long
	 * 
	 * @param value 5-bit or 6-bit value extracted from the instruction
	 * @param isValueA Must be true if decoding "a" value, otherwise false
	 * @return Decoded value
	 */

	private Short decodeValue(byte value, boolean isValueA) {
		Short retVal = null;
		
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
	        	pointer = USHORT_MASK & (gpRegs[value - 0x10] + pc++);
	        	modify = ModificationType.MEMORY;
	        	retVal = memory[pointer];
	        	cycle++;

	        // POP / PUSH
	        } else if (value == PUPO_VAL) {
	        	// POP / [SP++]
	        	if (isValueA) {
	        		modify = ModificationType.EMULATOR_ERROR; // POP can never be the target
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
	        	cycle++;
	        
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
	        	cycle++;

	        // next word literal
	        } else if (value == LNEXTW_VAL) {
	        	pointer = USHORT_MASK & pc++;
	        	modify = ModificationType.MEMORY;
	        	retVal = memory[pointer];
	        	
	        }	
			tempPointer = pointer;

		} else if (value < UPPER_BOUND){
			modify = ModificationType.NONE;  // writing to a literal must fail silently
			retVal = (short)(value - 0x21);  // literal value stored as signed short from -1 to 30
		}
		
		
		return retVal;
	
	}
	
	
	public void decodeInstruction(short word) {
		byte opcode = (byte) (0b011111 &  word);
		byte bi     = (byte) (0b011111 & (word >>  5));
		byte ai     = (byte) (0b111111 & (word >> 10));
			
		switch(opcode) {
		case 0x00:
			switch(bi) {
				case 0x00:           break; case 0x01:jsrOp(ai); break; 
				case 0x02:           break;	case 0x03:           break; 
				case 0x04:           break; case 0x05:           break;
				case 0x06:           break; case 0x07:           break; 
				case 0x08:intOp(ai); break; case 0x09:iagOp(ai); break; 
				case 0x0a:iasOp(ai); break; case 0x0b:rfiOp(ai); break;
				case 0x0c:iaqOp(ai); break; case 0x0d:           break;
				case 0x0e:           break;	case 0x0f:           break;
				case 0x10:hwnOp(ai); break; case 0x11:hwqOp(ai); break;
				case 0x12:hwiOp(ai); break; case 0x13:           break;
				case 0x14:           break;	case 0x15:           break; 
				case 0x16:           break; case 0x17:           break;	
				case 0x18:           break; case 0x19:           break; 
				case 0x1a:           break;	case 0x1b:           break; 
				case 0x1c:           break; case 0x1d:           break;
				case 0x1e:           break; case 0x1f:           break;			
			}
			break;
			case 0x01:setOp(bi, ai); break;	case 0x02:addOp(bi, ai); break;	
			case 0x03:subOp(bi, ai); break;	case 0x04:mulOp(bi, ai); break;	
			case 0x05:mliOp(bi, ai); break;	case 0x06:divOp(bi, ai); break; 
			case 0x07:dviOp(bi, ai); break;	case 0x08:modOp(bi, ai); break;	
			case 0x09:mdiOp(bi, ai); break;	case 0x0a:andOp(bi, ai); break;	
			case 0x0b:borOp(bi, ai); break;	case 0x0c:xorOp(bi, ai); break;	
			case 0x0d:shrOp(bi, ai); break;	case 0x0e:asrOp(bi, ai); break;	
			case 0x0f:shlOp(bi, ai); break;	case 0x10:ifbOp(bi, ai); break;	
			case 0x11:ifcOp(bi, ai); break;	case 0x12:ifeOp(bi, ai); break;
			case 0x13:ifnOp(bi, ai); break;	case 0x14:ifgOp(bi, ai); break;	
			case 0x15:ifaOp(bi, ai); break;	case 0x16:iflOp(bi, ai); break;	
			case 0x17:ifuOp(bi, ai); break;	case 0x18: 				 break;
			case 0x19: 				 break;	case 0x1a:adxOp(bi, ai); break;	
			case 0x1b:sbxOp(bi, ai); break;	case 0x1c:				 break; 
			case 0x1d:				 break;	case 0x1e:stiOp(bi, ai); break;
			case 0x1f:stdOp(bi, ai); break;
		}
	}
	
	

	private void hwiOp(byte ai) {
		
		
	}

	private void hwnOp(byte ai) {
		
		
	}

	private void hwqOp(byte ai) {
		
		
	}

	private void iaqOp(byte ai) {
		
		
	}

	private void iasOp(byte ai) {
		
		
	}

	private void iagOp(byte ai) {
		
		
	}

	private void rfiOp(byte ai) {
		
		
	}

	private void intOp(byte ai) {
		
		
	}

	private void jsrOp(byte ai) {
		short retVal = decodeValue(ai, true);
		memory[USHORT_MASK & --sp] = pc;
		pc = retVal;
	}

	private void stdOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void stiOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void sbxOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void adxOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifuOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void iflOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifaOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifgOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifnOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifeOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifcOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void ifbOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void shlOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		doWrite(source);
		
	}

	private void asrOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		
		target = (short) (source >>> (USHORT_MASK & target));
		doWrite(source);
		ex = (short) ((target << 16) >>> source);
		
	}

	private void shrOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		
		target = (short) ((USHORT_MASK & source) >>> (USHORT_MASK & target));
		doWrite(source);
		ex = (short) ((target << 16) >> source);
		
	}

	private void xorOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);

		target = (short) ((USHORT_MASK & source) ^ (USHORT_MASK & target));
		doWrite((short) target);
	}

	private void borOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);

		target = (short) ((USHORT_MASK & source) | (USHORT_MASK & target));
		doWrite((short) target);
	}

	private void andOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);

		target = (short) ((USHORT_MASK & source) & (USHORT_MASK & target));
		doWrite((short) target);
	}

	private void mdiOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		 
		target = source % target;
		doWrite((short) target);
	}

	private void modOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		 
		target = (USHORT_MASK & source) % (USHORT_MASK & target);
		doWrite((short) target);
		
	}

	private void dviOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		if (target != 0) {  
			target = source / target;
			doWrite((short) target);
			ex = (short) (target << 16 / source);
		} else {
			doWrite((short) 0);
			ex = 0;
		}
	}

	private void divOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		if (target != 0) {  
			target = (USHORT_MASK & source) / (USHORT_MASK & target);
			doWrite((short) target);
			ex = (short) (target << 16 / source);
		} else {
			doWrite((short) 0);
			ex = 0;
		}
	}

	private void mliOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		target *= source;
		doWrite((short) target);
		ex = (short) (target >> 16);
	}

	private void mulOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		target = (USHORT_MASK & source) * (USHORT_MASK & target);
		doWrite((short) target);
		ex = (short) (target >> 16);
		
	}

	private void subOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		target -= USHORT_MASK & source;
		doWrite((short) (USHORT_MASK & target));
		if (target < 0) ex = -1; // 0xFFFF;
		else ex = 0;
		
	}

	private void addOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		int target = decodeValue(bi, false);
		
		target += USHORT_MASK & source;
		doWrite((short) (USHORT_MASK & target));
		if (target < 0) ex = 1; 
		else ex = 0;
		
	}

	private void setOp(byte bi, byte ai) {
		short source = decodeValue(ai, true);
		short target = decodeValue(bi, false);
		
		target = source;
		doWrite(target);
	}


	private void doWrite(short value) {
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
			throw new RuntimeException("!!!FATAL!!! B value read as POP. This can't be.");
		default:
			assert(false) : "Check Modification Type enum";
			
		}
	}

	
	
}
