package mcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import analoginput.AnalogRangeData;
import analoginput.AnalogRangeData.AnalogType;

/**
 * Interface to Measurement Computing data acquisition boards. 
 * Interfaces to native source code (MS Windows only)
 * <p>
 * October 2019 this becomes obsolete with the new MCCJNA interface which 
 * used JNA. the JNA interface includes many more advanced functions for 
 * things like working out which analog ranges are actually available for 
 * a given board.  
 *  
 * @author Douglas Gillespie
 *
 */
public class MccJniInterface {

//
//	/* Selectable A/D Ranges codes */
//	public static final int BIP60VOLTS = 20; /* -60 to= 60 Volts */
//	public static final int BIP30VOLTS = 23;
//	public static final int BIP20VOLTS = 15; /* -20; to +20; Volts */
//	public static final int BIP15VOLTS = 21; /* -1; 5; to +1; 5; Volts */
//	public static final int BIP10VOLTS = 1; /* -1; 0; to +1; 0; Volts */
//	public static final int BIP5VOLTS = 0; /* -5; to +5; Volts */
//	public static final int BIP4VOLTS = 16; /* -4; to += 4; Volts */
//	public static final int BIP2PT5VOLTS = 2; /* -2.5; to +2.5; Volts */
//	public static final int BIP2VOLTS = 14; /* -2.0; to +2.0; Volts */
//	public static final int BIP1PT25VOLTS = 3; /* -1; .25; to +1; .25; Volts */
//	public static final int BIP1VOLTS = 4; /* -1; to +1; Volts */
//	public static final int BIPPT625VOLTS = 5; /* -.625; to +.625; Volts */
//	public static final int BIPPT5VOLTS = 6; /* -.5; to +.5; Volts */
//	public static final int BIPPT25VOLTS = 12; /* -0.25; to +0.25; Volts */
//	public static final int BIPPT2VOLTS = 13; /* -0.2; to +0.2; Volts */
//	public static final int BIPPT1VOLTS = 7; /* -.1; to +.1; Volts */
//	public static final int BIPPT05VOLTS = 8; /* -.05; to +.05; Volts */
//	public static final int BIPPT01VOLTS = 9; /* -.01; to +.01; Volts */
//	public static final int BIPPT005VOLTS = 10; /* -.005; to +.005; Volts */
//	public static final int BIP1PT67VOLTS = 11; /* -1; .67; to +1; .67; Volts */
//	public static final int BIPPT312VOLTS = 17; /* -0.31; 2; to +0.31; 2; Volts */
//	public static final int BIPPT156VOLTS = 18; /* -0.156; to +0.1; 56; Volts */
//	public static final int BIPPT125VOLTS = 22; /* -0.1; 25; to +0.1; 25; Volts */
//	public static final int BIPPT078VOLTS = 19; /* -0.078; to +0.078; Volts */


//
//	static public final int UNI10VOLTS      = 100;            /* 0 to 10 Volts*/
//	static public final int UNI5VOLTS       = 101;            /* 0 to 5 Volts */
//	static public final int UNI4VOLTS       = 114;            /* 0 to 4 Volts */
//	static public final int UNI2PT5VOLTS    = 102;            /* 0 to 2.5 Volts */
//	static public final int UNI2VOLTS       = 103;            /* 0 to 2 Volts */
//	static public final int UNI1PT67VOLTS   = 109;            /* 0 to 1.67 Volts */
//	static public final int UNI1PT25VOLTS   = 104;            /* 0 to 1.25 Volts */
//	static public final int UNI1VOLTS       = 105;            /* 0 to 1 Volt */
//	static public final int UNIPT5VOLTS     = 110;            /* 0 to .5 Volt */
//	static public final int UNIPT25VOLTS    = 111;            /* 0 to 0.25 Volt */
//	static public final int UNIPT2VOLTS     = 112;            /* 0 to .2 Volt */
//	static public final int UNIPT1VOLTS     = 106;            /* 0 to .1 Volt */
//	static public final int UNIPT05VOLTS    = 113;            /* 0 to .05 Volt */
//	static public final int UNIPT02VOLTS    = 108;            /* 0 to .02 Volt*/
//	static public final int UNIPT01VOLTS    = 107;            /* 0 to .01 Volt*/


//	private class MCCRangeComparatator implements Comparator<int> {
//
//		@Override
//		public int compare(Integer code1, Integer code2) {
//			AnalogRangeData ar1 = getRangeData(code1);
//			AnalogRangeData ar2 = getRangeData(code2);
//			return ar1.compareTo(ar2);
//		}
//		
//	}

	//	public static native boolean haveLibrary();

//	private native String jniGetBoardName(int iBoard);

//	private native double jniReadVoltage(int iBoard, int iChannel, int range);

//	private native int jniGetLastErrorCode();

//	private native String jniGetErrorString(int errorCode);

	//	private static final String SILIB = "../MccJniBorland/mccjni";
//	private static final String SILIB = "mccjniinterface";
//
//	private static boolean loadTried = false;
////	private static boolean loadOK = false;
//
//	private static final int MAXMCCBOARDS = 10;
//
//	public static final double MCCERRORVALUE = -99999; // matches a similar value in the C code

//	private ArrayList<MccBoardName> boardList;

//	public MccJniInterface() {
//		super();
//		loadLibrary();
//	}

//	public synchronized boolean loadLibrary() {
//
//		// just try loading once. If it fails the first time, it will also fail on
//		// subsequent calls. 
//		if (loadTried == false) {
//			try  {
//				System.loadLibrary(SILIB);
//				loadOK = true;
//			}
//			catch (UnsatisfiedLinkError e)
//			{
//				System.out.println ("native lib '" + SILIB + "' not found in 'java.library.path': "
//						+ System.getProperty ("java.library.path"));
//				loadOK = false;
//			}
//			loadTried = true;
//		}
//		if (loadOK && boardList == null) {
//			createBoardList();
//		}
//
//		return loadOK;
//	}

//	public int getNumBoards() {
//		// have to do this my listing and seeing which are empty. 
//		if (loadLibrary() == false) {
//			return 0;
//		}
//		//		if (boardList == null) {
//		//			createBoardList();
//		//		}
//		return boardList.size();
//	}
//
//	/**
//	 * Only ever called from loadlibrary. 
//	 */
//	private void createBoardList() {
//		boardList = new ArrayList();
//		String newName;
//		for (int i = 0; i < MAXMCCBOARDS; i++) {
//			newName = jniGetBoardName(i);
//			if (newName == null || newName.length() == 0) {
//				continue;
//			}
//			boardList.add(new MccBoardName(i, newName));
//		}
//	}
//
//	/**
//	 * Get a board name for a board index from PAMGuards internal list. Board indexes always start at 0.
//	 * @param iBoard board index
//	 * @return board name. 
//	 */
//	public String getBoardName(int iBoard) {
//		if (loadLibrary() == false) {
//			return null;
//		}
//		//		if (boardList == null) {
//		//			createBoardList();
//		//		}
//		if (iBoard < boardList.size()) {
//			return boardList.get(iBoard).getBoardName();
//		}
//		else {
//			return null;
//		}
//	}
//
//	public double readVoltage(int iBoard, int iChannel, int range) {
//		if (loadLibrary() == false) {
//			return 0;
//		}
//		int boardNumber = getBoardNumberFromIndex(iBoard);
//		if (boardNumber < 0) {
//			return 0;
//		}
//
//		double v = jniReadVoltage(boardNumber, iChannel, range);
//		//		System.out.println(String.format("Board %d channel %d, range %d, voltage %f",
//		//				iBoard, iChannel, range, v));
//		return v;
//	}
//
//	/**
//	 * Convert PAMGuards internal board index, which always starts at 0
//	 * to a MCC board number which may start at 0 or 1. 
//	 * @param boardIndex
//	 * @return
//	 */
//	private int getBoardNumberFromIndex(int boardIndex) {
//		if (boardIndex < boardList.size()) {
//			return boardList.get(boardIndex).getBoardNumber();
//		}
//		return -1;
//	}
//
//	public int getLastErrorCode() {
//		if (!loadLibrary()) {
//			return -1;
//		}
//		return jniGetLastErrorCode();
//	}
//
//	public String getErrorString(int errorCode) {
//		if (!loadLibrary()) {
//			return "No MCC Library";
//		}
//		return jniGetErrorString(errorCode);
//	}


}
