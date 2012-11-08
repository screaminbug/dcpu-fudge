package hr.tstrelar.dcpu;

public class Dcpu {
	short[] memory = new short[0x10000];
	short[] gpRegs = new short[8]; // A, B, C, X, Y, Z, I, J, in that order
	short sp, pc, ex, ia;
	long cycle;
	
	private class ValueDecoder {
		private int gpReg = -1;
		private int memWord = -1;
		private boolean needNextWord;
		private short value;  // this can only be from A value
		
		public ValueDecoder(byte valA) {
			this(valA, null);
		}
		public ValueDecoder(byte valA, Byte valB) {
			
			byte rawVal = 0;
			for (int i=0; i < 2; i++) {
			    if (i == 0) rawVal = valA;
			    else if (valB != null) rawVal = valB;
			    else break;
			    
				if (rawVal >= 0x00 && rawVal <= 0x1f) {
					// register
					if (rawVal <= 0x07) {
						gpReg = rawVal;
					}
					
					// [register]
			        else if (rawVal <= 0x0f) {
			        	value = gpRegs[rawVal - 0x07];
			        }
					
			        else if (rawVal <= 0x17) {
			        	gpReg = rawVal - 0x10;
			        	needNextWord = true;
			        	
			        } else if (rawVal == 0x18) {
			        	// if value a this is pop
			        	if (i==0) value = memory[sp++];  
			        	else memory[--sp] = value; 
			        } else if (rawVal == 0x19) {
			        	
			        } else if (rawVal == 0x1A) {
			        	
			        } else if (rawVal == 0x1B) {
			        	
			        } else if (rawVal == 0x1C) {
			        	
			        } else if (rawVal == 0x1D) {
			        	
			        } else if (rawVal == 0x1E) {
			        	
			        } else if (rawVal == 0x1F) {
			        	
			        }	       
					
				} else {
					
				}
			}
		}
	}
	
	
	
	public void decodeInstruction(short word) {
		byte opcode = (byte) (0b011111 &  word);
		byte bi     = (byte) (0b011111 & (word >>  5));
		byte ai     = (byte) (0b111111 & (word >> 10));
		
		System.out.printf("opcode = %d, a = %d, b = %d", opcode, ai, bi); 
				
		switch(opcode) {
		case 0x00:
			switch(bi) {
			case 0x00:           break; case 0x01:jsrOp(ai); break; case 0x02:           break;
			case 0x03:           break; case 0x04:           break; case 0x05:           break;
			case 0x06:           break; case 0x07:           break; case 0x08:intOp(ai); break;
			case 0x09:iagOp(ai); break; case 0x0a:iasOp(ai); break; case 0x0b:rfiOp(ai); break;
			case 0x0c:iaqOp(ai); break; case 0x0d:           break; case 0x0e:           break;
			case 0x0f:           break; case 0x10:hwnOp(ai); break; case 0x11:hwqOp(ai); break;
			case 0x12:hwiOp(ai); break; case 0x13:           break; case 0x14:           break;
			case 0x15:           break; case 0x16:           break; case 0x17:           break;
			case 0x18:           break; case 0x19:           break; case 0x1a:           break;
			case 0x1b:           break; case 0x1c:           break; case 0x1d:           break;
			case 0x1e:           break; case 0x1f:           break;			
			}
			break;
		case 0x01:setOp(bi, ai); break;	case 0x02:addOp(bi, ai); break;	case 0x03:subOp(bi, ai); break;
		case 0x04:mulOp(bi, ai); break;	case 0x05:mliOp(bi, ai); break;	case 0x06:divOp(bi, ai); break;
		case 0x07:dviOp(bi, ai); break;	case 0x08:modOp(bi, ai); break;	case 0x09:mdiOp(bi, ai); break;
		case 0x0a:andOp(bi, ai); break;	case 0x0b:borOp(bi, ai); break;	case 0x0c:xorOp(bi, ai); break;
		case 0x0d:shrOp(bi, ai); break;	case 0x0e:asrOp(bi, ai); break;	case 0x0f:shlOp(bi, ai); break;
		case 0x10:ifbOp(bi, ai); break;	case 0x11:ifcOp(bi, ai); break;	case 0x12:ifeOp(bi, ai); break;
		case 0x13:ifnOp(bi, ai); break;	case 0x14:ifgOp(bi, ai); break;	case 0x15:ifaOp(bi, ai); break;
		case 0x16:iflOp(bi, ai); break;	case 0x17:ifuOp(bi, ai); break;	case 0x18: 				 break;
		case 0x19: 				 break;	case 0x1a:adxOp(bi, ai); break;	case 0x1b:sbxOp(bi, ai); break;
		case 0x1c:				 break; case 0x1d:				 break;	case 0x1e:stiOp(bi, ai); break;
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
		
		
	}

	private void stdOp(byte bi, byte ai) {
		
		
	}

	private void stiOp(byte bi, byte ai) {
		
		
	}

	private void sbxOp(byte bi, byte ai) {
		
		
	}

	private void adxOp(byte bi, byte ai) {
		
		
	}

	private void ifuOp(byte bi, byte ai) {
		
		
	}

	private void iflOp(byte bi, byte ai) {
		
		
	}

	private void ifaOp(byte bi, byte ai) {
		
		
	}

	private void ifgOp(byte bi, byte ai) {
		
		
	}

	private void ifnOp(byte bi, byte ai) {
		
		
	}

	private void ifeOp(byte bi, byte ai) {
		
		
	}

	private void ifcOp(byte bi, byte ai) {
		
		
	}

	private void ifbOp(byte bi, byte ai) {
		
		
	}

	private void shlOp(byte bi, byte ai) {
		
		
	}

	private void asrOp(byte bi, byte ai) {
		
		
	}

	private void shrOp(byte bi, byte ai) {
		
		
	}

	private void xorOp(byte bi, byte ai) {
		
		
	}

	private void borOp(byte bi, byte ai) {
		
		
	}

	private void andOp(byte bi, byte ai) {
		
		
	}

	private void mdiOp(byte bi, byte ai) {
		
		
	}

	private void modOp(byte bi, byte ai) {
		
		
	}

	private void dviOp(byte bi, byte ai) {
		
		
	}

	private void divOp(byte bi, byte ai) {
		
		
	}

	private void mliOp(byte bi, byte ai) {
		
		
	}

	private void mulOp(byte bi, byte ai) {
		
		
	}

	private void subOp(byte bi, byte ai) {
		
		
	}

	private void addOp(byte bi, byte ai) {
		
		
	}

	private void setOp(byte bi, byte ai) {
		// register
		if (ai == ValueType.REGISTER ) {
			switch (ValueTypeif (bi == ValueType.REGISTER)
				gpRegs[ai] = gpRegs[bi];
			else
		}
		
	}
	
}
