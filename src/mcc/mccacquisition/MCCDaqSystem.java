package mcc.mccacquisition;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AcquisitionParameters;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamguardMVC.debug.Debug;
import analoginput.AnalogRangeData;
import mcc.MccJniInterface;
import mcc.mccjna.MCCBoardInfo;
import mcc.mccjna.MCCConstants;
import mcc.mccjna.MCCException;
import mcc.mccjna.MCCJNA;
import mcc.mccjna.MCCUtils;
import mcc.mccjna.MCCJNA.MCCLibrary;
import warnings.PamWarning;
import warnings.WarningSystem;
import wavFiles.ByteConverter;
import wavFiles.ByteConverterWavInt16;

public class MCCDaqSystem extends DaqSystem implements PamSettings {
	
	public static final String systemName = "Measurement Computing Devices";
	public static final String systemType = "MCC DAQ Boards";
		
	private MCCDaqParams mccDaqParams = new MCCDaqParams();
	
	private MCCAcquisitionDialogPanel mccDialogPanel;
	
	private AcquisitionControl acquisitionControl;
	private MCCBoardInfo runningBoard;
//	private int inputBuffSize;
//	private volatile byte[] inputBuffer;
	private Thread readThreadThread;
	private volatile boolean keepRunning;
	private Pointer inputPointer;
	private int inputBufferFrames;
	private int chunkFrames;
	private int nChan;
	private ByteConverter byteconverter = new ByteConverterWavInt16();
	private Integer analogRange;
	private AnalogRangeData currentRangeData;
	private long totalSampleCount;
	
	private PamWarning mccWarning = new PamWarning("MCC Acquisition", "", 0);
	
	/**
	 * @param acquisitionControl 
	 * 
	 */
	public MCCDaqSystem(AcquisitionControl acquisitionControl) {
		super();
		this.acquisitionControl = acquisitionControl;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getSystemType() {
		return systemType;
	}

	@Override
	public String getSystemName() {
		return systemName;
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		if (mccDialogPanel == null) {
			mccDialogPanel = new MCCAcquisitionDialogPanel(acquisitionDialog, this);
		}
		return mccDialogPanel.getComponent();
	}

	@Override
	public void dialogSetParams() {
		mccDialogPanel.setParams();
	}

	@Override
	public boolean dialogGetParams() {
		return mccDialogPanel.getParams();
	}

	@Override
	public int getMaxSampleRate() {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public int getMaxChannels() {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return PARAMETER_UNKNOWN;
	}
	
	private void setWarning(String warning, int level) {
		if (warning == null) {
			WarningSystem.getWarningSystem().removeWarning(mccWarning);
		}
		else {
			mccWarning.setWarningMessage(warning);
			mccWarning.setWarnignLevel(level);
			WarningSystem.getWarningSystem().addWarning(mccWarning);
		}
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		MCCLibrary mccLib = MCCJNA.getMccLibrary();
		if (mccLib == null) {
			setWarning("No Measurement Computing software installed", 2);
			return false;
		}
		runningBoard = getSelectdBoard();
		if (runningBoard == null) {
			setWarning("No Measurement Computing board selected", 2);
			return false;
		}
		currentRangeData = mccDaqParams.getRangeData();
		analogRange = MCCUtils.findRangeCode(currentRangeData);
		if (analogRange == null) {
			setWarning("No Measurement Computing input range data", 2);
		}
		totalSampleCount = 0;
		
		AcquisitionParameters mainParams = acquisitionControl.acquisitionParameters;
		nChan = mainParams.getNChannels();
		int fs = (int) mainParams.getSampleRate();
		chunkFrames = fs/10; // about 1/10s in a chunk. 
		chunkFrames /= 256; // must also be divisible by 256 to suit board block sizes
		chunkFrames = Math.max(chunkFrames, 2);
		chunkFrames *= 256;
		
		int nChunks = 10;	// want about 10 of these chunks (so about a 1s buffer).
		inputBufferFrames = chunkFrames*nChunks;
//		inputBuffSize = inputBufferSamples*nChan*2;
		inputPointer = mccLib.cbWinBufAlloc(new NativeLong(inputBufferFrames*nChan));
//		inputBuffer = (byte[]) inputPointer. 
		
		int ans = mccLib.cbAInputMode(runningBoard.getBoardNumber(), mccDaqParams.differential ? MCCConstants.DIFFERENTIAL : MCCConstants.SINGLE_ENDED);
		if (ans != MCCConstants.NOERRORS && ans != MCCConstants.AIINPUTMODENOTCONFIGURABLE) {
			String msg = MCCJNA.getErrorMessage(ans);
			setWarning("Measurement Computing Error: " + msg, 2);
			return false;
		}
				
		NativeLongByReference sampleRate = new NativeLongByReference(new NativeLong(fs));
		int options = MCCConstants.CONTINUOUS | MCCConstants.BACKGROUND;
//		int ans2 = mccLib.cbEnableEvent(runningBoard.getBoardNumber(), MCCConstants.ON_DATA_AVAILABLE, chunkSamples, new SamplesCallback(), null);
		ans = mccLib.cbAInScan(runningBoard.getBoardNumber(), 0, nChan-1, new NativeLong(inputBufferFrames*nChan), sampleRate, 
				analogRange, inputPointer, options);
		if (ans != MCCConstants.NOERRORS) {
			String msg = MCCJNA.getErrorMessage(ans);
			setWarning("Measurement Computing Error: " + msg, 2);
			return false;
		}
		else {
			setWarning(null, 0);
		}
//		Debug.out.printf("Prep enableEvent:%d, buff samples :%d size %d\n", ans, inputBufferSamples, inputBufferSamples);
		keepRunning = true;
		/**
		 * Start thread to read the data. 
		 */
		readThreadThread = new Thread(new ReadThread(mccLib));
		readThreadThread.start();
		return true;
	}
	
	private class ReadThread implements Runnable {
		private MCCLibrary mccLib;

		public ReadThread(MCCLibrary mccLib) {
			this.mccLib = mccLib;
		}

		public void run() {
			int currentPos = 0; // position in read buffer
			int chunkSamps = chunkFrames*nChan; // total samples to read each go. 
			int maxPos = inputBufferFrames*nChan;
			long nextCount = chunkSamps;
			ShortByReference status = new ShortByReference();
			NativeLongByReference curCount = new NativeLongByReference();
			NativeLongByReference curIndex = new NativeLongByReference();
			int lastCount = 0;
			long totalCount = 0;
			
			while (keepRunning) {

				int ans = mccLib.cbGetIOStatus(runningBoard.getBoardNumber(), status, curCount, curIndex, MCCConstants.AIFUNCTION);
				int intcount = curCount.getValue().intValue();
				int count = intcount-lastCount;
				/**
				 * lets say that intcount has just wrapper, then it should still
				 * be the case that intcount-lastCount will return a +ve number.
				 * If both have wrapped, then the difference will still be positive.  
				 */
				totalCount += (long) count;
				lastCount = intcount;
			
				if (totalCount > nextCount) {
					// read the data
					short[] data = inputPointer.getShortArray(currentPos*2, chunkSamps);
					repackData(data);
					nextCount += chunkSamps;
					currentPos += chunkSamps;
					if (currentPos >= maxPos) {
						currentPos = 0;
					}
//					System.out.printf("IOStatus: retn %d status %d, curCount %d, curIndex %d\n", ans, status.getValue(), curCount.getValue(), curIndex.getValue());
				}
				else {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	} 

//	public class SamplesCallback implements EVENTCALLBACK {
//
//		@Override
//		public void callback(int a, int b, int c, Pointer p) {
//			System.out.printf("Samples callback a=%d,b=%d,c=%d\n",a,b,c);
//		}
//		
//	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		return true;
	}

	public void repackData(short[] data) {
		/**
		 * Scaling very odd, with it looking like it's an unsigned integer returned, where 0 will be -FS 
		 * and 2^nBits-1 will be +FS. All gets quite complicated, so easiest to pass to MCC's own function 
		 * which converts to volts. Will then have to rescale to -1:1 scale since PAMguard will 
		 * expect to add the range calibration later. 
		 */
		short[] chanTags = new short[data.length];
		int nSamp = data.length / nChan;
		double[][] engUnits = new double[nChan][nSamp];
		MCCLibrary mccLibrary = MCCJNA.getMccLibrary();
		mccLibrary.cbAConvertData(runningBoard.getBoardNumber(), new NativeLong(data.length), data, chanTags);
//		FloatByReference fbr = new FloatByReference();
		FloatByReference floatVal = new FloatByReference();
		double rMax = currentRangeData.getRange()[1];
		long millis = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSampleCount);
		for (int iCh = 0; iCh < nChan; iCh++) {
			for (int i = iCh, j = 0; i < data.length; i+=nChan, j++) {
				//			data[i] = Short.reverseBytes(data[i]);
				mccLibrary.cbToEngUnits(runningBoard.getBoardNumber(), analogRange, data[i], floatVal);
				engUnits[iCh][j] = floatVal.getValue()/rMax;
			}
			RawDataUnit dataUnit = new RawDataUnit(millis, 1<<iCh, totalSampleCount, nSamp);
			dataUnit.setRawData(engUnits[iCh], true);
			acquisitionControl.getAcquisitionProcess().getNewDataQueue().addNewData(dataUnit);
		}
		totalSampleCount += nSamp;
//		double[][] doubleData = new double[nChan][nSamp];
//		byteconverter.bytesToDouble(data, doubleData, data.length);
//		for (int i = 0; i < nChan; i++) {
//			
//		}
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		keepRunning = false;
		if (runningBoard == null) {
			return;
		}
		MCCLibrary mccLibrary = MCCJNA.getMccLibrary();
		if (mccLibrary != null) {
			int ans = mccLibrary.cbStopIOBackground(runningBoard.getBoardNumber(), MCCConstants.AIFUNCTION);
			if (ans != 0) {
				Debug.out.printf("cbStopIOBackground returned %d\n", ans);
			}
		}
		
		if (readThreadThread != null) {
			try {
				readThreadThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			readThreadThread = null;
		}
		if (mccLibrary != null) {
			mccLibrary.cbWinBufFree(inputPointer);
		}
		
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return false;
	}

	@Override
	public int getDataUnitSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDeviceName() {
		MCCBoardInfo currboard = getSelectdBoard();
		if (currboard == null) {
			return null;
		}
		else {
			return currboard.getBoardName();
		}
	}
	
	/**
	 * 
	 * @return Currently selected board
	 */
	public MCCBoardInfo getSelectdBoard() {
		ArrayList<MCCBoardInfo> boardList = MCCJNA.getBoardInformation();
		if (mccDaqParams.boardIndex>=0 && mccDaqParams.boardIndex<boardList.size()) {
			return boardList.get(mccDaqParams.boardIndex);
		}
		else {
			return null;
		}
	}

//	/**
//	 * @return the mccJna
//	 */
//	public MCCJNA getMccJna() {
//		if (triedLoad == false) {
//			try {
//				mccJna = new MCCJNA();
//			} catch (MCCException e) {
//				
//			}
//			triedLoad = true;
//		}
//		return mccJna;
//	}

	/**
	 * @return the mccDaqParams
	 */
	public MCCDaqParams getMccDaqParams() {
		return mccDaqParams;
	}

	/**
	 * @param mccDaqParams the mccDaqParams to set
	 */
	public void setMccDaqParams(MCCDaqParams mccDaqParams) {
		this.mccDaqParams = mccDaqParams;
	}

	@Override
	public String getUnitName() {
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return systemType;
	}

	@Override
	public Serializable getSettingsReference() {
		return mccDaqParams;
	}

	@Override
	public long getSettingsVersion() {
		return MCCDaqParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mccDaqParams = (MCCDaqParams) pamControlledUnitSettings.getSettings();
		return true;
	}

}
