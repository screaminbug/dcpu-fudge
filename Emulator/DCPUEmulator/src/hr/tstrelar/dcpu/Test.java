package hr.tstrelar.dcpu;

public class Test {
	public static void main(String[] args) {
		Dcpu dcpu = new Dcpu();
		
		dcpu.decodeInstruction((short)0b101010_11001_01101);
	}

}
