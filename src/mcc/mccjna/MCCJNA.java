package mcc.mccjna;

import java.util.ArrayList;
import java.util.Arrays;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * JNA interface for Measurement Computing acquisition boards.
 * <p>
 * Implements C functions documented at https://www.mccdaq.com/pdfs/manuals/Mcculw_WebHelp/ULStart.htm
 * <p> 
 * @author Doug Gillespie
 *
 */
public class MCCJNA {

	private static MCCLibrary mccLibrary;

	private static long loadNanos;

	private static ArrayList<MCCBoardInfo> boardInformation;
	
	private static String loadError;
	
	static {
		/*
		 * Will load on first call of any function in MCCJNA
		 */
		loadMCCJna();
	}
	
	/**
	 * Get the error string returned if the UL fails to load (will probably be because 
	 * the library couldn't be found on the system, but may occur if the library changes
	 * are we try to load functions that no longer exist). 
	 * @return
	 */
	public static String getJnaLoadError() {
		return loadError;
	}
	
	/**
	 * Load the library and link to JNA
	 * @return true if library loaded OK
	 */
	private static boolean loadMCCJna() {
		loadNanos = System.nanoTime();

		try {
			mccLibrary = (MCCLibrary) Native.loadLibrary(choseLibrary(), MCCLibrary.class);
		}
		catch (Error e) {
			mccLibrary = null;
			loadError = "Unable to load MeasurementComputing library: " + e.getMessage();
			return false;
		}

		loadNanos = System.nanoTime()-loadNanos;
		
//		getDeviceInventory();
		
		return mccLibrary != null;
	}

	/**
	 * Select the correct library name for different platforms. 
	 * May be able to extend this for other operating systems. 
	 * There **may** be a UL for Linux and Max. 
	 * @return Name of the universal library. 
	 */
	private static String choseLibrary() {
		if (Platform.isWindows()) {
			if (Platform.is64Bit()) {
				return "cbw64";
			}
			else {
				return "cbw32";
			}
		}
		return "";
	}
	
//	/**
//	 * Get a device inventory using a call to cbGetDaqDeviceInventory
//	 * <p>Does not include demo boards. 
//	 * @return array of device information.
//	 */
//	public static DaqDeviceDescriptor[] getDeviceInventory() {
//		// try to get the device inventory. 
//		int maxDevs = 10;
//		IntByReference nDevices = new IntByReference(maxDevs);
//		DaqDeviceDescriptor deviceDescriptor = new DaqDeviceDescriptor();
//		DaqDeviceDescriptor[] deviceDescriptors = (DaqDeviceDescriptor[]) deviceDescriptor.toArray(maxDevs);
//		int ans = mccLibrary.cbGetDaqDeviceInventory(DaqDeviceDescriptor.ANY_IFC, deviceDescriptors, nDevices);
//		int nDev = nDevices.getValue();
//		deviceDescriptors = Arrays.copyOf(deviceDescriptors, nDev);
//		return deviceDescriptors;
//	}

	/**
	 * 
	 * @return  a list of information on all installed boards. Returns null if no MCC library is available
	 */
	public static ArrayList<MCCBoardInfo> getBoardInformation() {
		if (mccLibrary == null) {
			return null;
		}
		if (boardInformation == null) {
			findBoardInformation();
		}
		return boardInformation;
	}

	/**
	 * Find information for all installed boards 
	 * @return information for all installed boards. 
	 */
	private static synchronized int findBoardInformation() {
		if (mccLibrary == null) {
			return 0;
		}
		boardInformation = new ArrayList<>();
		int maxboards = 10;
		byte[] boardNamebytes = new byte[MCCConstants.BOARDNAMELEN];
		for (int iBoard = 0; iBoard <= maxboards; iBoard++) {
			int ans = mccLibrary.cbGetBoardName(iBoard, boardNamebytes);
			if (ans == MCCConstants.NOERRORS) {
				MCCBoardInfo mccBoardinfo = new MCCBoardInfo(boardInformation.size(), iBoard, new String(boardNamebytes).trim());
//				checkRanges(mccBoardinfo);
				boardInformation.add(mccBoardinfo);
			}
		}
		return boardInformation.size();
	}


	/**
	 * Get an error message in a string format. 
	 * @param errCode error code
	 * @return Error string
	 */
	public static String getErrorMessage(int errCode) {
		if (mccLibrary == null) {
			return "MCC Library not installed";
		}
		byte[] msg = new byte[MCCConstants.ERRSTRLEN];
		int err = mccLibrary.cbGetErrMsg(errCode, msg);
		if (err != MCCConstants.NOERRORS) {
			return "Unknown MCC Error Code: " + errCode;
		}
		else {
			return new String(msg).trim();
		}
	}

	/**
	 * Interface to the MCCLibrary JNA calls. Note that these have to carefully match the 
	 * calls in the C Universal library provided by MeasurementComputing. Each probably needs
	 * testing to make sure it works for both 332 and 64 bit platforms. While all calls 
	 * have been copy pasted from the C header file, I've commented all of them out apart from 
	 * the few that i've tested / used so far.  <p>
	 * Constants from the header file are in MCCConstants
	 * @author Doug Gillespie
	 *
	 */
	public interface MCCLibrary extends Library {
		/*
		 * What was 'long' in C might be 32 or 64 bit depending on platform
		 * so have to use NativeLong.  
		 */
//
//		typedef void (__stdcall *EVENTCALLBACK)(int, unsigned, unsigned, void*);
		public interface EVENTCALLBACK extends Callback {
			public void callback(int a, int b, int c, Pointer p);
		}
//
//		public int cbACalibrateData (int BoardNum, long NumPoints, int Gain, 
//				USHORT *ADData);
		public int cbGetRevision (FloatByReference RevNum, FloatByReference VxDRevNum);
//		public int cbLoadConfig(char *CfgFileName);
//		public int cbSaveConfig(char *CfgFileName);
		public int cbAConvertData (int BoardNum, NativeLong NumPoints, short[] ADData, 
				short[] ChanTags);
//		public int cbAConvertPretrigData (int BoardNum, long PreTrigCount, 
//				long TotalCount, USHORT *ADData, 
//				USHORT *ChanTags);
		public int cbAIn (int BoardNum, int Chan, int Gain, ShortByReference DataValue);
		public int cbAIn32 (int BoardNum, int Chan, int Gain, LongByReference DataValue, int Options);
		public int cbAInScan (int BoardNum, int LowChan, int HighChan, NativeLong Count,
				NativeLongByReference Rate, int Gain, Pointer MemHandle, int Options);
//		public int cbALoadQueue (int BoardNum, short *ChanArray, short *GainArray, 
//				int NumChans);
//		public int cbAOut (int BoardNum, int Chan, int Gain, USHORT DataValue);
//		public int cbAOutScan (int BoardNum, int LowChan, int HighChan, 
//				long Count, long *Rate, int Gain, 
//				HGLOBAL MemHandle, int Options);
//		public int cbAPretrig (int BoardNum, int LowChan, int HighChan,
//				long *PreTrigCount, long *TotalCount, long *Rate, 
//				int Gain, HGLOBAL MemHandle, int Options);
//		public int cbATrig (int BoardNum, int Chan, int TrigType, 
//				USHORT TrigValue, int Gain, USHORT *DataValue);
//		public int cbC7266Config (int BoardNum, int CounterNum, int Quadrature,
//				int CountingMode, int DataEncoding, int IndexMode,
//				int InvertIndex, int FlagPins, int GateEnable);
//		public int cbC8254Config (int BoardNum, int CounterNum, int Config);
//		public int cbC8536Config (int BoardNum, int CounterNum, int OutputControl,
//				int RecycleMode, int TrigType);
//		public int cbC9513Config (int BoardNum, int CounterNum, int GateControl,
//				int CounterEdge, int CountSource, 
//				int SpecialGate, int Reload, int RecycleMode, 
//				int BCDMode, int CountDirection, 
//				int OutputControl);
//		public int cbC8536Init (int BoardNum, int ChipNum, int Ctr1Output);
//		public int cbC9513Init (int BoardNum, int ChipNum, int FOutDivider, 
//				int FOutSource, int Compare1, int Compare2, 
//				int TimeOfDay);
//		public int cbCFreqIn (int BoardNum, int SigSource, int GateInterval,
//				USHORT *Count, long *Freq);
//		public int cbCIn (int BoardNum, int CounterNum, USHORT *Count);
//		public int cbCIn32 (int BoardNum, int CounterNum, ULONG *Count);
//		public int cbCIn64 (int BoardNum, int CounterNum, ULONGLONG *Count);
//		public int cbCLoad (int BoardNum, int RegNum, unsigned int LoadValue);
//		public int cbCLoad32 (int BoardNum, int RegNum, ULONG LoadValue);
//		public int cbCLoad64 (int BoardNum, int RegNum, ULONGLONG LoadValue);
//		public int cbCStatus (int BoardNum, int CounterNum, ULONG *StatusBits);
//		public int cbCStoreOnInt (int BoardNum, int IntCount, short *CntrControl,
//				HGLOBAL MemHandle);
//		public int cbCInScan(int BoardNum, int FirstCtr,int LastCtr, LONG Count,
//				LONG *Rate, HGLOBAL MemHandle, ULONG Options);
//		public int cbCConfigScan(int BoardNum, int CounterNum, int Mode,int DebounceTime,
//				int DebounceMode, int EdgeDetection,
//				int TickSize, int MappedChannel);
//		public int cbCClear (int BoardNum, int CounterNum);
//		public int cbTimerOutStart (int BoardNum, int TimerNum, double *Frequency);
//		public int cbTimerOutStop (int BoardNum, int TimerNum);
//		public int cbPulseOutStart (int BoardNum, int TimerNum, double *Frequency, double *DutyCycle, unsigned int PulseCount, double *InitialDelay, int IdleState, int Options);
//		public int cbPulseOutStop (int BoardNum, int TimerNum);
//		public int cbDBitIn (int BoardNum, int PortType, int BitNum, 
//				USHORT *BitValue);
//		public int cbDBitOut (int BoardNum, int PortType, int BitNum, USHORT BitValue);
//		public int cbDConfigPort (int BoardNum, int PortType, int Direction);
//		public int cbDConfigBit (int BoardNum, int PortType, int BitNum, int Direction);
//		public int cbDIn (int BoardNum, int PortType, USHORT *DataValue);
//		public int cbDIn32 (int BoardNum, int PortType, UINT *DataValue);
//		public int cbDInScan (int BoardNum, int PortType, long Count, long *Rate,
//				HGLOBAL MemHandle, int Options);
//		public int cbDOut(int BoardNum, int PortType, USHORT DataValue);
//		public int cbDOut32(int BoardNum, int PortType, UINT DataValue);
//		public int cbDOutScan (int BoardNum, int PortType, long Count, long *Rate,
//				HGLOBAL MemHandle, int Options);
//		public int cbDInArray (int BoardNum, int LowPort, int HighPort, ULONG *DataArray);
//		public int cbDOutArray (int BoardNum, int LowPort, int HighPort, ULONG *DataArray);
//		public int cbDClearAlarm (int BoardNum, int PortType, UINT Mask);
//		public int cbErrHandling (int ErrReporting, int ErrHandling);
//		public int cbFileAInScan (int BoardNum, int LowChan, int HighChan,
//				long Count, long *Rate, int Gain, 
//				char *FileName, int Options);
//		public int cbFileGetInfo (char *FileName, short *LowChan, short *HighChan,
//				long *PreTrigCount, long *TotalCount, 
//				long *Rate, int *Gain);
//		public int cbFilePretrig (int BoardNum, int LowChan, int HighChan,
//				long *PreTrigCount, long *TotalCount, 
//				long *Rate, int Gain, char *FileName, 
//				int Options);
//		public int cbFileRead (char *FileName, long FirstPoint, long *NumPoints,
//				USHORT *DataBuffer);
		public int cbFlashLED(int BoardNum);
		public int cbGetErrMsg (int ErrCode, byte[] ErrMsg);
		public int cbGetIOStatus (int BoardNum, ShortByReference Status, NativeLongByReference CurCount,
				NativeLongByReference CurIndex, int FunctionType);
//		public int cbRS485 (int BoardNum, int Transmit, int Receive);
		public int cbStopIOBackground (int BoardNum, int FunctionType);
//		public int cbTIn (int BoardNum, int Chan, int Scale, float *TempValue,
//				int Options);
//		public int cbTInScan (int BoardNum, int LowChan, int HighChan, int Scale,
//				float *DataBuffer, int Options);
//		public int cbMemSetDTMode (int BoardNum, int Mode);
//		public int cbMemReset (int BoardNum);
//		public int cbMemRead (int BoardNum, USHORT *DataBuffer, long FirstPoint, 
//				long Count);
//		public int cbMemWrite (int BoardNum, USHORT *DataBuffer,long FirstPoint, 
//				long Count);
//		public int cbMemReadPretrig (int BoardNum, USHORT *DataBuffer,
//				long FirstPoint, long Count);
//		public int cbWinBufToArray (HGLOBAL MemHandle, USHORT *DataArray, 
//				long FirstPoint, long Count);
//		public int cbWinBufToArray32 (HGLOBAL MemHandle, ULONG *DataArray, 
//				long FirstPoint, long Count);
//
//		public int cbWinBufToArray64 (HGLOBAL MemHandle, ULONGLONG *DataArray, 
//				long FirstPoint, long Count);
//
//		HGLOBAL EXTCCONV cbScaledWinBufAlloc (long NumPoints);
//		public int cbScaledWinBufToArray (HGLOBAL MemHandle, double *DataArray, 
//				long FirstPoint, long Count);
//
//		public int cbWinArrayToBuf (USHORT *DataArray, HGLOBAL MemHandle, 
//				long FirstPoint, long Count);
//		public int cbWinArrayToBuf32 (ULONG *DataArray, HGLOBAL MemHandle, 
//				long FirstPoint, long Count);
//		public int cbScaledWinArrayToBuf (double *DataArray, HGLOBAL MemHandle, 
//				long FirstPoint, long Count);
//
		Pointer cbWinBufAlloc (NativeLong NumPoints);
		Pointer cbWinBufAlloc32 (NativeLong NumPoints);
		Pointer cbWinBufAlloc64 (NativeLong NumPoints);
//
		public int cbWinBufFree (Pointer MemHandle);
//		public int cbInByte (int BoardNum, int PortNum);
//		public int cbOutByte (int BoardNum, int PortNum, int PortVal);
//		public int cbInWord (int BoardNum, int PortNum);
//		public int cbOutWord (int BoardNum, int PortNum, int PortVal);
//
//		public int cbGetConfig (int InfoType, int BoardNum, int DevNum, 
//				int ConfigItem, int *ConfigVal);
//		public int cbGetConfigString (int InfoType, int BoardNum, int DevNum, 
//				int ConfigItem, char* ConfigVal, int* maxConfigLen);
//
//		public int cbSetConfig (int InfoType, int BoardNum, int DevNum, 
//				int ConfigItem, int ConfigVal);
//		public int cbSetConfigString (int InfoType, int BoardNum, int DevNum, 
//				int ConfigItem, char* ConfigVal, int* configLen);
//
		public int cbToEngUnits (int BoardNum, int Range, short DataVal, 
				FloatByReference EngUnits);
		public int cbToEngUnits32 (int BoardNum, int Range, NativeLong DataVal, 
				DoubleByReference EngUnits);
//		public int cbFromEngUnits (int BoardNum, int Range, float EngUnits, 
//				USHORT *DataVal);
		public int cbGetBoardName (int BoardNum, byte[] BoardName);
		public int cbDeclareRevision(FloatByReference RevNum);
//		public int cbSetTrigger (int BoardNum, int TrigType, USHORT LowThreshold, 
//				USHORT HighThreshold);
//
//		public int cbEnableEvent(int BoardNum, unsigned EventType, unsigned Count, 
//				EVENTCALLBACK CallbackFunc, void *UserData);
//
//		public int cbDisableEvent(int BoardNum, unsigned EventType);
//		public int cbSelectSignal(int BoardNum,  int Direction, int Signal, int Connection, int Polarity);
//		public int cbGetSignal(int BoardNum, int Direction, int Signal, int Index, int* Connection, int* Polarity);
//
//		public int cbSetCalCoeff(int BoardNum, int FunctionType, int Channel, int Range, int Item, int Value, int Store);
//		public int cbGetCalCoeff(int BoardNum, int FunctionType, int Channel, int Range, int Item, int* Value);
//
//
//		// Get log file name
//
//		// store the preferences
//		public int cbLogSetPreferences(int timeFormat, int timeZone, int units);
//
//		// get the preferences
//		public int cbLogGetPreferences(int* timeFormat, int* timeZone, int* units);
//
//		// Get log file name
//		public int cbLogGetFileName(int fileNumber, char* path, char* filename);
//
//		// Get info for log file
//		public int cbLogGetFileInfo(char* filename, int* version, int* fileSize);
//
//		// Get sample info for log file
//		public int cbLogGetSampleInfo(char* filename, int* sampleInterval, int* sampleCount, 
//				int* startDate, int* startTime);
//
//		// Get the AI channel count for log file
//		public int cbLogGetAIChannelCount(char* filename, int* aiCount);
//
//		// Get AI info for log file
//		public int cbLogGetAIInfo(char* filename, int* channelNumbers, int* units);
//
//		// Get CJC info for log file
//		public int cbLogGetCJCInfo(char* filename, int* cjcCount);
//
//		// Get DIO info for log file
//		public int cbLogGetDIOInfo(char* filename, int* dioCount);
//
//		// read the time tags to an array
//		public int cbLogReadTimeTags(char* filename, int startSample, int count, int* dateTags, int*timeTags);
//
//		// read the analog data to an array
//		public int cbLogReadAIChannels(char* filename, int startSample, int count, float* analog);
//
//		// read the CJC data to an array
//		public int cbLogReadCJCChannels(char* filename, int startSample, int count, float* cjc);
//
//		// read the DIO data to an array
//		public int cbLogReadDIOChannels(char* filename, int startSample, int count, int* dio);
//
//		// convert the log file to a .TXT or .CSV file
//		public int cbLogConvertFile(char* srcFilename, char* destFilename, int startSample, int count, int delimiter);
//
//		public int cbDaqInScan(int BoardNum, short *ChanArray, short *ChanTypeArray, short *GainArray, int ChanCount, long *Rate,
//				long *PretrigCount, long *TotalCount, HGLOBAL MemHandle, int Options);
//		public int cbDaqSetTrigger(int BoardNum, int TrigSource, int TrigSense, int TrigChan, int ChanType, 
//				int Gain, float Level, float Variance, int TrigEvent);
//		public int cbDaqSetSetpoints (int BoardNum, float *LimitAArray, float *LimitBArray, float *Reserved, int *SetpointFlagsArray,
//				int *SetpointOutputArray, float *Output1Array, float *Output2Array, float *OutputMask1Array,
//				float *OutputMask2Array, int SetpointCount);
//
//		public int cbDaqOutScan(int BoardNum, short *ChanArray, short *ChanTypeArray, short *GainArray, int ChanCount, long *Rate,
//				long Count, HGLOBAL MemHandle, int Options);
//		public int cbGetTCValues(int BoardNum, short *ChanArray, short *ChanTypeArray, int ChanCount, HGLOBAL MemHandle, int FirstPoint,
//				long Count, int Scale, float *TempValArray);
//
		public int cbVIn (int BoardNum, int Chan, int Gain, FloatByReference DataValue, int Options);
		public int cbVIn32 (int BoardNum, int Chan, int Gain, DoubleByReference DataValue, int Options);
//		public int cbVOut (int BoardNum, int Chan, int Gain, float DataValue, int Options);
//
//		public int cbDeviceLogin(int BoardNum, char* AccountName, char* Password);
//		public int cbDeviceLogout(int BoardNum);
//
//		public int cbTEDSRead(int BoardNum, int Chan, BYTE* DataBuffer, long *Count, int Options);
//
		public int cbAInputMode(int BoardNum, int InputMode);
		public int cbAChanInputMode(int BoardNum, int Chan, int InputMode);
//
//		public int cbIgnoreInstaCal();
//		public int cbCreateDaqDevice(int BdNum, DaqDeviceDescriptor DeviceDescriptor);
//		public int cbGetDaqDeviceInventory(int InterfaceType, DaqDeviceDescriptor[] Inventory, IntByReference NumberOfDevices);
//		public int cbReleaseDaqDevice(int BdNum);
//		public int cbGetBoardNumber (DaqDeviceDescriptor DeviceDescriptor);
//		public int cbGetNetDeviceDescriptor(CHAR* Host, INT Port, DaqDeviceDescriptor* DeviceDescriptor, INT Timeout);

		public int cbEnableEvent(int BoardNum, int EventType, int Count, 
				EVENTCALLBACK CallbackFunc, Pointer UserData);
	}


	/**
	 * @return the mccLibrary
	 */
	public static MCCLibrary getMccLibrary() {
		return mccLibrary;
	}

	/**
	 * @return the loadNanos
	 */
	public static long getLoadNanos() {
		return loadNanos;
	}
}
