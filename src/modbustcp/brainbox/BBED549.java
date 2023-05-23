package modbustcp.brainbox;

import analoginput.AnalogRangeData;
import analoginput.AnalogRangeData.AnalogType;
import modbustcp.ModbusData;
import modbustcp.ModbusTCP;
import modbustcp.ModbusTCPException;

public class BBED549 {
	
	// MESSAGES
	public static final int DISCRETE_INPUTS_READ = 2;
	public static final int COILS_READ = 1;
	public static final int INPUT_REGISTERS_READ = 4;
	public static final int HOLDING_REGISTERS_READ = 3;
	public static final int COILS_WRITE_SINGLE = 5;
	public static final int HOLDING_REGISTERS_WRITE_SINGLE = 6;
	public static final int COILS_WRITE_MULTIPLE = 0x15; // IS THIS SUPPOSED TO 0X10 OR OX15 ?
	public static final int HOLDING_REGISTERS_WRITE_MULTIPLE = 0x16; // DITTO !
	// REGISTER ADDRESSES
	public static final int AI_INT_HOLDING_REGISTERS = 0X0;
	public static final int AI_INT_INPUT_REGISTERS = 0X0;
	public static final int AI_FLOAT_HOLDING_REGISTERS = 0X0020;
	public static final int AI_FLOAT_INPUT_REGISTERS = 0X0020;
	public static final int AI_ERROR_FLAGS = 0x0400;
	public static final int AI_CHANNEL_ENABLE = 0x0040;
	public static final int AI_INPUT_RANGE = 0x0060;
	public static final int AI_INTEGER_FORMAT = 0x0080;
	// INPUT RANGE CODES
	public static final int VOLTS_2pt5 = 5;
	public static final int mAMP_20 = 6;
	public static final int mAMP_20b = 0x0D;
	public static final int mAMP_4_20 = 7;
	public static final int VOLTS_10 = 8;
	public static final int VOLTS_5 = 9;
	public static final int VOLTS_1 = 4;
	public static final int VOLTS_1b = 0x0A;
	public static final int VOLTS_0pt5 = 3;
	public static final int VOLTS_0pt5b = 0x0B;
	public static final int VOLTS_0pt15 = 0x0C;
	public static final int mAMP_0_20 = 0x1A;
	public static final int VOLTS_0pt075 = 0x3A;
	public static final int VOLTS_0pt250 = 0x3B;
	private ModbusTCP mtcp;
	
	public static final int[] ALLRANGES = {VOLTS_10, VOLTS_5, VOLTS_1, VOLTS_0pt5, mAMP_0_20, mAMP_4_20, mAMP_20};
	
	/**
	 * Create a Modbus TCP connnection to the BainBoxes ED-549
	 * @param ipAddress
	 * @throws ModbusTCPException
	 */
	public BBED549(String ipAddress) throws ModbusTCPException {
		mtcp = ModbusTCP.openSocket(ipAddress);
	}
	
	/**
	 * Close the connection. Will need to build a new
	 * object to reopen the connection. 
	 */
	public void close() {
		if (mtcp != null) {
			mtcp.close();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	public ModbusData setInputRange(int channel, int range) throws ModbusTCPException {
		ModbusData d1 = mtcp.sendCommand(BBED549.HOLDING_REGISTERS_WRITE_SINGLE, BBED549.AI_INPUT_RANGE+channel, range);
		return d1;
	}
	
	public short readRawAnalogChannel(int channel) throws ModbusTCPException {
		ModbusData modbusData = mtcp.sendCommand(INPUT_REGISTERS_READ, AI_INT_HOLDING_REGISTERS + channel, 1);
		if (modbusData.getDataBytes() != 2) {
			throw new ModbusTCPException("Invalid data read from Modbus, bytes = " + modbusData.getDataBytes());
		}
		short[] shortData = modbusData.getShortData();
		return shortData[0];
	}

	public short[] readRawAnalogChannels(int firstChannel, int nChannels) throws ModbusTCPException {
		ModbusData modbusData = mtcp.sendCommand(INPUT_REGISTERS_READ, AI_INT_HOLDING_REGISTERS + firstChannel, nChannels);
		if (modbusData.getDataBytes() != 2 * nChannels) {
			throw new ModbusTCPException("Invalid data read from Modbus, bytes = " + modbusData.getDataBytes());
		}
		short[] shortData = modbusData.getShortData();
		return shortData;
	}
	
	

//	public double readFloatAnalogChannel(int channel, int range) throws ModbusTCPException {
////			float tVal = 7.077f;
////			int  iBits = Float.floatToIntBits(tVal);
////			System.out.printf("Text float val %3.3f to 0x%x\n", tVal, iBits);
////	//		channel = 0;
////	//		ModbusData ctrlData = mtcp.sendCommand(6, AI_INTEGER_FORMAT, 0x0, 1);
////	//		ModbusData ctrlData2 = mtcp.sendCommand(5, AI_INTEGER_FORMAT, 0x1, 1);
////	//		channel = channel * 2;
////			ModbusData modbusData = mtcp.sendCommand(INPUT_REGISTERS_READ, AI_FLOAT_HOLDING_REGISTERS + channel, 2);
////			float[] shortData = modbusData.getFloatData();
////			ModbusData modbusDatar = mtcp.sendCommand(INPUT_REGISTERS_READ, AI_FLOAT_HOLDING_REGISTERS + channel, 4);
////			float[] shortDatar = modbusDatar.getRevFloatData();
////			System.out.printf("Float %s, Rev Float %s\n", new Float(shortData[0]), new Float(shortDatar[0]));
//			
////			return shortData[0];
//		}

	/**
	 * convert integer data array to engineering units. 
	 * @param range range on device, assumed the same for all channels. 
	 * @param data data array
	 * @return  data in engineers units of Volts or Amps (not bothering with mV and mA - stick to SI)
	 */
	public double[] hexToEngineering(int range, short[] data) {
		if (data == null) {
			return null;
		}
		int n = data.length;
		double [] engData = new double[n];
		for (int i = 0; i < n; i++) {
			engData[i] = hexToEngineering(range, data[i]);
		}
		return engData;
	}


	/**
	 * convert integer data to engineering units. 
	 * @param range range on device, assumed the same for all channels. 
	 * @param data data array
	 * @return  data in engineers units of Volts or Amps (not bothering with mV and mA - stick to SI)
	 */
	public static double hexToEngineering(int range, int data) {
		/**
		 * convert integer data to engineering units. 
		 * @param range range on device, assumed the same for all channels. 
		 * @param data data array
		 * @return
		 */
		double min = 0;
		double max = 1;
		double intMin = Short.MIN_VALUE;
		double intMax = Short.MAX_VALUE;
		switch(range) {
		// INPUT RANGE CODES
		case VOLTS_2pt5:
			min = -2.5;
			max = +2.5;
			break;
		case mAMP_20:
			min = -0.02;
			max = 0.02;
			break;
		case mAMP_20b:
			min = -.02;
			max = .02;
			break;
		case mAMP_4_20:
			min = .004;
			max = .02;
			intMin = 0;
			intMax = 65536;
			break;
		case VOLTS_10:
			min = -10;
			max = 10;
			break;
		case VOLTS_5:
			min = -5;
			max = 5;
			break;
		case VOLTS_1:
			min = -1;
			max = 1;
			break;
		case VOLTS_1b:
			min = -1;
			max = 1;
			break;
		case VOLTS_0pt5:
			min = -.5;
			max = -.5;
			break;
		case VOLTS_0pt5b:
			min = -.5;
			max = .5;
			break;
		case VOLTS_0pt15:
			min = -.15;
			max = .15;
			break;
		case mAMP_0_20:
			min = 0;
			max = .02;
			intMin = 0;
			intMax = 65536;
			break;
		case VOLTS_0pt075:
			min = -.075;
			max = .075;
			break;
		case VOLTS_0pt250:
			min = -.25;
			max = .25;
			break;	
		}
		if (intMin == 0 && data < 0) {
			data += 65536;
		}
		double deltaF = max-min;
		double deltaI = intMax-intMin;
		double doubleVal = ((double) data-intMin)/deltaI*deltaF+min;
		return doubleVal;
	}


	/**
	 * Find the internal code that matches this rangeData. <br>
	 * This is super inefficient, so don't call too often !
	 * @param rangeData PAMGaurd range data
	 * @return BB range code. or Null if the range not found
	 */
	public static Integer getBBRangeCode(AnalogRangeData rangeData) {
//		allRanges = ALLRANGES;
		if (rangeData == null) {
			return null;
		}
		for (int i = 0; i < ALLRANGES.length; i++) {
			AnalogRangeData rD = getRangeData(ALLRANGES[i]);
			if (rD == null) continue;
			if (rD.equals(rangeData)) {
				return ALLRANGES[i];
			}
		}
		return null;
	}

	/**
	 * Convert one of the internally used range codes into 
	 * a standard PAMGuard analog range. 
	 * @param range BrainBox range code
	 * @return PAMGuard range object. 
	 */
	public static AnalogRangeData getRangeData(int range) {
		/**
		 * convert integer data to engineering units. 
		 * @param range range on device, asumed the same for all channels. 
		 * @param data data array
		 * @return
		 */
		double min = 0;
		double max = 1;
		AnalogRangeData.AnalogType type = AnalogType.VOLTS;
		switch(range) {
		// INPUT RANGE CODES
		case VOLTS_2pt5:
			min = -2.5;
			max = +2.5;
			break;
		case mAMP_20:
			min = -0.02;
			max = 0.02;
			type = AnalogType.AMPS;
			break;
		case mAMP_20b:
			min = -.02;
			max = .02;
			type = AnalogType.AMPS;
			break;
		case mAMP_4_20:
			min = .004;
			max = .02;
			type = AnalogType.AMPS;
			break;
		case VOLTS_10:
			min = -10;
			max = 10;
			break;
		case VOLTS_5:
			min = -5;
			max = 5;
			break;
		case VOLTS_1:
			min = -1;
			max = 1;
			break;
		case VOLTS_1b:
			min = -1;
			max = 1;
			break;
		case VOLTS_0pt5:
			min = -.5;
			max = -.5;
			break;
		case VOLTS_0pt5b:
			min = -.5;
			max = .5;
			break;
		case VOLTS_0pt15:
			min = -.15;
			max = .15;
			break;
		case mAMP_0_20:
			min = 0;
			max = .02;
			type = AnalogType.AMPS;
			break;
		case VOLTS_0pt075:
			min = -.075;
			max = .075;
			break;
		case VOLTS_0pt250:
			min = -.25;
			max = .25;
			break;	
		}
		double[] r = {min, max};
		return new AnalogRangeData(r, type);
	}

	
	public static String sayInputRange(int rangeCode) {
		switch (rangeCode) {
		case VOLTS_2pt5:
			return "+/-2.5V";
		case mAMP_20:
		case mAMP_20b:
			return "+/-20mA";
		case mAMP_4_20:
			return "4-20mA";
		case VOLTS_10:
			return "+/-10V";
		case VOLTS_5:
			return "+/-5V";
		case VOLTS_1:
		case VOLTS_1b:
			return "+/-1V";
		case VOLTS_0pt5:
		case VOLTS_0pt5b:
			return "+/-500mV";
		case VOLTS_0pt15:
			return "+/-150mV";
		case mAMP_0_20:
			return "0-20mA";
		case VOLTS_0pt075:
			return "+/-75mV";
		case VOLTS_0pt250:
			return "+/-250mV";
		}
		return "Unknown Code " + rangeCode;
	}


}
