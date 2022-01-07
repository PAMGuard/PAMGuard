package mcc.mccjna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;

import analoginput.AnalogRangeData;
import analoginput.AnalogRangeData.AnalogType;
import mcc.MccJniInterface;
import mcc.mccjna.MCCJNA.MCCLibrary;

/**
 * Useful functions that use the JNI but use some PAMGuard specific
 * structures, so don't want them in the JNA class in order that that
 * can be used with other software. <p>
 * Mostly Static functions. 
 * @author Doug Gillespie
 *
 */
public class MCCUtils {

	public static int[] getAvailableRanges(int boardNum, int terminalConfig, boolean continuous, boolean unipolar, boolean bipolar) {
		int[] availableRanges = new int[0];
		if (unipolar) {
			int[] seRanges = getAvailableRanges(boardNum, terminalConfig, continuous, MCCConstants.uniranges);
			availableRanges = seRanges;
		}
		if (bipolar) {
			int[] biRanges = getAvailableRanges(boardNum, terminalConfig, continuous, MCCConstants.bipolarRanges);
			int nSE = availableRanges.length;
			availableRanges = Arrays.copyOf(availableRanges, nSE + biRanges.length);
			System.arraycopy(biRanges, 0, availableRanges, nSE, biRanges.length);
		}
		
		return availableRanges;
	}
	
	public static int[] getAvailableRanges(int boardNum, int terminalConfig, boolean continuous, int[] rangeList) {
		int[] ranges = new int[rangeList.length];
		int nOK = 0;
		for (int i = 0; i < rangeList.length; i++) {
			if (isAvailableRange(boardNum, terminalConfig, continuous, rangeList[i])) {
				ranges[nOK++] = rangeList[i];
			}
		}
		
		return Arrays.copyOf(ranges, nOK);
	}
	
	/**
	 * Turn a board index (0 to ...) to a board number. Board numbers probably start at 1 !
	 * @param boardIndex board index
	 * @return board number or -1 if the board doesn't exist. 
	 */
	public static int boardIndexToNumber(int boardIndex) {
		ArrayList<MCCBoardInfo> boardInfos = MCCJNA.getBoardInformation();
		if (boardInfos == null || boardIndex >= boardInfos.size() || boardIndex < 0) {
			return -1;
		}
		else {
			return boardInfos.get(boardIndex).getBoardNumber();
		}
	}

	/**
	 * Read a single analogue voltage using cbAIn(...)
	 * @param board board number
	 * @param channel channel number
	 * @param differential true if input is differential. 
	 * @param range Analog gain range
	 * @return voltage (converted to volts)
	 * @throws MCCException thrown if there is any error. 
	 */
	public static double readVoltage(int board, int channel, boolean differential, int range) throws MCCException{
		MCCLibrary mccLib = MCCJNA.getMccLibrary();
		if (mccLib == null) {
			throw new MCCException(MCCJNA.getJnaLoadError());
		}
		int ans = mccLib.cbAInputMode(board, differential ? MCCConstants.DIFFERENTIAL : MCCConstants.SINGLE_ENDED);
		if (ans != MCCConstants.NOERRORS && ans != MCCConstants.AIINPUTMODENOTCONFIGURABLE) {
			throw new MCCException(MCCJNA.getErrorMessage(ans));
		}
		
		return readVoltage(board, channel, range);
	}
	
	/**
	 * Read a single analogue voltage using cbAIn(...)
	 * @param board board number
	 * @param channel channel number
	 * @param range Analog gain range
	 * @return voltage (converted to volts)
	 * @throws MCCException thrown if there is any error. 
	 */
	public static double readVoltage(int board, int channel, int range) throws MCCException{
		MCCLibrary mccLib = MCCJNA.getMccLibrary();
		if (mccLib == null) {
			throw new MCCException(MCCJNA.getJnaLoadError());
		}
		ShortByReference val = new ShortByReference();
		int ans = mccLib.cbAIn(board, channel, range, val);
		if (ans != MCCConstants.NOERRORS) {
			throw new MCCException(MCCJNA.getErrorMessage(ans));
		}
		FloatByReference volts = new FloatByReference();
		ans = mccLib.cbToEngUnits(board, range, val.getValue(), volts);
		if (ans != MCCConstants.NOERRORS) {
			throw new MCCException(MCCJNA.getErrorMessage(ans));
		}
		return volts.getValue();
	}

	/**
	 * Work out where in the list of bipolar ranges a particular range is. 
	 * Used for setting combo box list positions. 
	 * @param range bipolar MCC range
	 * @return index in bipolarRanges list. or -1 if range not found. 
	 */
	static public int getBipolarRangeIndex(int range) {
		for (int i = 0; i < MCCConstants.bipolarRanges.length; i++) {
			if (MCCConstants.bipolarRanges[i] == range) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Go from analog range data to a code understood by MCC. 
	 * @param rangeData PAMGuard range data
	 * @return MCC range code. 
	 */
	public static Integer findRangeCode(AnalogRangeData rangeData) {
		if (rangeData == null) {
			return null;
		}
		for (int i = 0; i < MCCConstants.bipolarRanges.length; i++) {
			AnalogRangeData rd = getRangeData(MCCConstants.bipolarRanges[i]);
			if (rd == null) continue;
			if (rd.equals(rangeData)) {
				return MCCConstants.bipolarRanges[i];
			}
		}
		return null;
	}

	/**
	 * Get a Range Data object. Note that unipolar and bipolar are NOT
	 * the same as single ended and differential input. i.e you can have 
	 * single ended bipolar input and differential single ended input if you 
	 * so wish
	 * @param rangeCode
	 * @return analog Range Data object. 
	 */
	public static AnalogRangeData getRangeData(int rangeCode) {
		switch (rangeCode) {
		/*
		 * Unipolar Ranges
		 */
		case MCCConstants.UNI10VOLTS:
			return AnalogRangeData.makeSingleEndRange(10, AnalogType.VOLTS);
		case MCCConstants.UNI5VOLTS:
			return AnalogRangeData.makeSingleEndRange(5, AnalogType.VOLTS);
		case MCCConstants.UNI4VOLTS:
			return AnalogRangeData.makeSingleEndRange(4, AnalogType.VOLTS);
		case MCCConstants.UNI2PT5VOLTS:
			return AnalogRangeData.makeSingleEndRange(2.5, AnalogType.VOLTS);
		case MCCConstants.UNI2VOLTS:
			return AnalogRangeData.makeSingleEndRange(2, AnalogType.VOLTS);
		case MCCConstants.UNI1PT67VOLTS:
			return AnalogRangeData.makeSingleEndRange(1.67, AnalogType.VOLTS);
		case MCCConstants.UNI1PT25VOLTS:
			return AnalogRangeData.makeSingleEndRange(1.25, AnalogType.VOLTS);
		case MCCConstants.UNI1VOLTS:
			return AnalogRangeData.makeSingleEndRange(1, AnalogType.VOLTS);
		case MCCConstants.UNIPT5VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.5, AnalogType.VOLTS);
		case MCCConstants.UNIPT25VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.25, AnalogType.VOLTS);
		case MCCConstants.UNIPT2VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.2, AnalogType.VOLTS);
		case MCCConstants.UNIPT1VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.1, AnalogType.VOLTS);
		case MCCConstants.UNIPT05VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.05, AnalogType.VOLTS);
		case MCCConstants.UNIPT02VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.02, AnalogType.VOLTS);
		case MCCConstants.UNIPT01VOLTS:
			return AnalogRangeData.makeSingleEndRange(0.01, AnalogType.VOLTS);
			/*
			 * Bipolar Ranges
			 */
		case MCCConstants.BIP60VOLTS      :       
			return AnalogRangeData.makeBipolarRange(60, AnalogType.VOLTS);
		case MCCConstants.BIP30VOLTS      :       
			return AnalogRangeData.makeBipolarRange(30, AnalogType.VOLTS);
		case MCCConstants.BIP20VOLTS      :       
			return AnalogRangeData.makeBipolarRange(20, AnalogType.VOLTS);
		case MCCConstants.BIP15VOLTS      :       
			return AnalogRangeData.makeBipolarRange(15, AnalogType.VOLTS);
		case MCCConstants.BIP10VOLTS      :         
			return AnalogRangeData.makeBipolarRange(10, AnalogType.VOLTS);
		case MCCConstants.BIP5VOLTS       :          
			return AnalogRangeData.makeBipolarRange(5, AnalogType.VOLTS);
		case MCCConstants.BIP4VOLTS       :          
			return AnalogRangeData.makeBipolarRange(4, AnalogType.VOLTS);
		case MCCConstants.BIP2PT5VOLTS    :           
			return AnalogRangeData.makeBipolarRange(2.5, AnalogType.VOLTS);
		case MCCConstants.BIP2VOLTS       :          
			return AnalogRangeData.makeBipolarRange(2, AnalogType.VOLTS);
		case MCCConstants.BIP1PT25VOLTS   :          
			return AnalogRangeData.makeBipolarRange(1.25, AnalogType.VOLTS);
		case MCCConstants.BIP1VOLTS       :          
			return AnalogRangeData.makeBipolarRange(1., AnalogType.VOLTS);
		case MCCConstants.BIPPT625VOLTS   :          
			return AnalogRangeData.makeBipolarRange(.625, AnalogType.VOLTS);
		case MCCConstants.BIPPT5VOLTS     :        
			return AnalogRangeData.makeBipolarRange(.5, AnalogType.VOLTS);
		case MCCConstants.BIPPT25VOLTS    :           
			return AnalogRangeData.makeBipolarRange(.25, AnalogType.VOLTS);
		case MCCConstants.BIPPT2VOLTS     :         
			return AnalogRangeData.makeBipolarRange(.2, AnalogType.VOLTS);
		case MCCConstants.BIPPT1VOLTS     :        
			return AnalogRangeData.makeBipolarRange(.1, AnalogType.VOLTS);
		case MCCConstants.BIPPT05VOLTS    :      
			return AnalogRangeData.makeBipolarRange(.05, AnalogType.VOLTS);
		case MCCConstants.BIPPT01VOLTS    :      
			return AnalogRangeData.makeBipolarRange(.01, AnalogType.VOLTS);
		case MCCConstants.BIPPT005VOLTS   :     
			return AnalogRangeData.makeBipolarRange(.005, AnalogType.VOLTS);
		case MCCConstants.BIP1PT67VOLTS   :      
			return AnalogRangeData.makeBipolarRange(1.67, AnalogType.VOLTS);
		case MCCConstants.BIPPT312VOLTS   :      
			return AnalogRangeData.makeBipolarRange(0.312, AnalogType.VOLTS);
		case MCCConstants.BIPPT156VOLTS   :      
			return AnalogRangeData.makeBipolarRange(0.156, AnalogType.VOLTS);
		case MCCConstants.BIPPT125VOLTS   :      
			return AnalogRangeData.makeBipolarRange(0.125, AnalogType.VOLTS);
		case MCCConstants.BIPPT078VOLTS   :      
			return AnalogRangeData.makeBipolarRange(0.078, AnalogType.VOLTS);
		default:
			return null;
		}
	}
	
	static public String sayBibolarRange(int range) {
		AnalogRangeData ard = getRangeData(range);
		if (ard != null) {
			return ard.toString();
		}
		switch (range) {
		case MCCConstants.BIP20VOLTS      :       
			return "-20 to +20 Volts";
		case MCCConstants.BIP10VOLTS      :         
			return "-10 to +10 Volts";
		case MCCConstants.BIP5VOLTS       :          
			return "-5 to +5 Volts";
		case MCCConstants.BIP4VOLTS       :          
			return "-4 to + 4 Volts";
		case MCCConstants.BIP2PT5VOLTS    :           
			return "-2.5 to +2.5 Volts";
		case MCCConstants.BIP2VOLTS       :          
			return "-2.0 to +2.0 Volts";
		case MCCConstants.BIP1PT25VOLTS   :          
			return "-1.25 to +1.25 Volts";
		case MCCConstants.BIP1VOLTS       :            
			return "-1 to +1 Volts";
		case MCCConstants.BIPPT625VOLTS   :          
			return "-.625 to +.625 Volts";
		case MCCConstants.BIPPT5VOLTS     :        
			return "-.5 to +.5 Volts";
		case MCCConstants.BIPPT25VOLTS    :           
			return "-0.25 to +0.25 Volts";
		case MCCConstants.BIPPT2VOLTS     :          
			return "-0.2 to +0.2 Volts";
		case MCCConstants.BIPPT1VOLTS     :        
			return "-.1 to +.1 Volts";
		case MCCConstants.BIPPT05VOLTS    :      
			return "-.05 to +.05 Volts";
		case MCCConstants.BIPPT01VOLTS    :      
			return "-.01 to +.01 Volts";
		case MCCConstants.BIPPT005VOLTS   :     
			return "-.005 to +.005 Volts";
		case MCCConstants.BIP1PT67VOLTS   :      
			return "-1.67 to +1.67 Volts";
		default:
			return "Unknown range";
		}
	}
	/**
	 * Convert MCC range codes to a list of PAMGuard AnalogRangeData objects.
	 * @param rangeCodes range codes
	 * @return list of range data objects. 
	 */
	public static ArrayList<AnalogRangeData> getAnalogRangeData(int[] rangeCodes) {
		if (rangeCodes == null) {
			return null;
		}
		ArrayList<AnalogRangeData> rangeData = new ArrayList<>(rangeCodes.length);
		for (int i = 0; i < rangeCodes.length; i++) {
			AnalogRangeData ard = getRangeData(rangeCodes[i]);
			if (ard != null) {
				rangeData.add(ard);
			}
		}
		return rangeData;
	}

	/**
	 * Test to see if an input range is available for a given board for continuous or 
	 * single value acquisition
	 * @param boardNum board number
	 * @param terminalConfig single ended or differential (MCCConstants.SINGLE_ENDED or MCCConstatns.DIFFERENTIAL)
	 * @param continuous true for continuous acquisition, false otherwise
	 * @param range MCC range constant
	 * @return true if it seems to acquire OK. 
	 */
	public static boolean isAvailableRange(int boardNum, int terminalConfig, boolean continuous, int range) {
		MCCLibrary mccLib = MCCJNA.getMccLibrary();
		if (mccLib == null) {
			return false;
		}
		int ans;
		ans = mccLib.cbAInputMode(boardNum, terminalConfig);
		if (ans != MCCConstants.NOERRORS && ans != MCCConstants.AIINPUTMODENOTCONFIGURABLE) {
			return false;
		}
		if (continuous) {
			return isAvailableContinuousRange(boardNum, range);
		}
		else {
			return isAvailableRange(boardNum, range);
		}
	}

	/**
	 * Get all analogue ranges for optionally bipolar and single ended operation. 
	 * @param bipolar 
	 * @param unipolar
	 * @return list of all available ranges for any board. 
	 */
	public static ArrayList<AnalogRangeData> getAllRanges(boolean bipolar, boolean unipolar) {
		ArrayList<AnalogRangeData> allRanges = new ArrayList<>();
		if (bipolar) for (int i = 0; i < MCCConstants.bipolarRanges.length; i++) {
			allRanges.add(getRangeData(MCCConstants.bipolarRanges[i]));
		}
		if (unipolar) for (int i = 0; i < MCCConstants.uniranges.length; i++) {
			allRanges.add(getRangeData(MCCConstants.uniranges[i]));
		}
		Collections.sort(allRanges);
		return allRanges;
	}

	/**
	 * Test to see if a range is available for continuous acquisitoin using a given board, which will 
	 * already have been set to single ended or differential input. 
	 * @param boardNum board number
	 * @param range single ended or differential range
	 * @return true if the board can acquire data using cbAInScan
	 */
	private static boolean isAvailableContinuousRange(int boardNum, int range) {
		MCCLibrary mccLib = MCCJNA.getMccLibrary();
		NativeLongByReference rate = new NativeLongByReference(new NativeLong(1000));
		int nSamp = 128;
		NativeLong count = new NativeLong(nSamp);
		Pointer memHandle = mccLib.cbWinBufAlloc(count); 
		int options = MCCConstants.CONTINUOUS | MCCConstants.BACKGROUND;
		int ans = mccLib.cbAInScan(boardNum, 0, 0, count, rate, range, memHandle, options);
		if (ans == MCCConstants.NOERRORS) {
			mccLib.cbStopIOBackground(boardNum, MCCConstants.AIFUNCTION);
		}
		mccLib.cbWinBufFree(memHandle);
		return (ans == MCCConstants.NOERRORS);
	}

	/**
	 * Test to see if a range is available for single value acquisition using a given board, which will 
	 * already have been set to single ended or differential input. 
	 * @param boardNum board number
	 * @param range single ended or differential range
	 * @return true if the board can acquire data using cbAIn
	 */
	private static boolean isAvailableRange(int boardNum, int range) {
		ShortByReference dataVal = new ShortByReference();
		int ans = MCCJNA.getMccLibrary().cbAIn(boardNum, 0, range, dataVal);
		return (ans == MCCConstants.NOERRORS);
	}
	
}
