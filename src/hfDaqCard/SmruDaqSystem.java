package hfDaqCard;

import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;

public class SmruDaqSystem extends DaqSystem implements PamSettings {

	protected SmruDaqParameters smruDaqParameters = new SmruDaqParameters(newCardName);

	private SmruDaqDialogPanel smruDaqDialogPanel;

	private AcquisitionControl daqControl;

	private SmruDaqJNI smruDaqJNI;

	private AudioDataQueue newDataUnits;

	private int wantedSamples;

	private int nDaqCards = 0;

	public static final String newCardName = "SAIL DAQ Card";

	private final String daqCardName = newCardName;

	private int prepareErrors;

	public static final String oldCardName = "SMRU Ltd DAQ Card";

	public static final int VERBOSELEVEL = 0;
	

	/**
	 * @param daqControl
	 */
	public SmruDaqSystem(AcquisitionControl daqControl) {
		super();
		this.daqControl = daqControl;

		smruDaqJNI = new SmruDaqJNI(this);

		nDaqCards = smruDaqJNI.getnDevices();
		for (int i = 0; i < nDaqCards; i++) {
			for (int l = 0; l < 2; l++) {
				smruDaqJNI.setLED(i, l,	0);
			}
		}

		smruDaqDialogPanel = new SmruDaqDialogPanel(this);
		PamSettingManager.getInstance().registerSettings(this);

	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean select) {
		if (select) {
			//			smruDaqJNI.enumerateDevices(SmruDaqJNI.RESET_AUTO);
//			smruDaqJNI.checkDevices(false);
		}
	}
	/**
	 * Wrapper round modules terminalPrint. 
	 * @param str
	 * @param verboseLevel
	 */
	protected void terminalPrint(String str, int verboseLevel) {
		if (verboseLevel <= VERBOSELEVEL) {
			System.out.println(str);
		}
	}
	
	public String getJNILibInfo() {
		String lib = smruDaqJNI.getLibrary();
		int libVer = smruDaqJNI.getLibarayVersion();
		String verStr;
		if (libVer < 0) {
			verStr = String.format("%s not loaded", lib);
		}
		else {
			verStr = String.format("%s.dll Version %d", lib, libVer);
		}
		return verStr;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return true;
	}

	@Override
	public boolean dialogGetParams() {
		SmruDaqParameters newParams = smruDaqDialogPanel.getParams();
		if (newParams != null) {
			smruDaqParameters = newParams;
			return true;
		}
		return false;
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (smruDaqParameters.systemType==null) smruDaqParameters.systemType=getSystemType();

		smruDaqDialogPanel.setParams(smruDaqParameters);
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(
			AcquisitionDialog acquisitionDialog) {
		smruDaqDialogPanel.setDaqDialog(acquisitionDialog);
		return smruDaqDialogPanel.getDialogPanel();
	}

	@Override
	public int getDataUnitSamples() {
		return wantedSamples;
	}

	@Override
	public String getDeviceName() {
		return daqCardName;
	}

	@Override
	public int getMaxChannels() {
		return DaqSystem.PARAMETER_FIXED;
	}

	@Override
	public int getMaxSampleRate() {
		return DaqSystem.PARAMETER_FIXED; // will disable the built in sampel rate data. 
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return SmruDaqParameters.VPEAKTOPEAK;
	}

	@Override
	public String getSystemName() {
		return daqCardName;
	}

	@Override
	public String getSystemType() {
		return daqCardName;
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		wantedSamples = 1<<12;
		while (wantedSamples < daqControl.getAcquisitionParameters().sampleRate/20) {
			wantedSamples *= 2;
		}

		int totalUsed = smruDaqParameters.getNumUsedBoards(nDaqCards);

		int nPrepped = prepareDaqCards(false);
		if (nPrepped != totalUsed) {
			System.out.println(String.format("Problem with %d out of %d SAIL daq cards. perform full reset", totalUsed-nPrepped, totalUsed));
			nPrepped = prepareDaqCards(true);
		}
		if (nPrepped != totalUsed) {
			String msg = String.format("One or more SAIL DAQ cards cannot be accessed. This may be because they are already open"
					+ " in another instance of PAMGuard. \n"
					+ "Check that no other instances of PAMGuard are open and try again. \nIf no other instances of PAMGuard are open "
					+ "then you should cycle the power on the card(s) and restart PAMGuard");
			if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
				System.out.println(msg);
			}
			else {
				JOptionPane.showMessageDialog(daqControl.getGuiFrame(), msg, daqControl.getUnitName() + " Error", JOptionPane.ERROR_MESSAGE);
			}
			PamController.getInstance().stopLater();
			return false;
		}
		return true;
	}

	/**
	 * Prepare daq cards, giving them a full reset if necessary. 
	 * @param fullReset
	 * @return number of Daq cards successfully initialised. 
	 */
	int prepareDaqCards(boolean fullReset) {
		prepareErrors = 0;
		int nPrepped = 0;

		int totalUsed = smruDaqParameters.getNumUsedBoards(nDaqCards);
		int nUsed = 0;

		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {

			if (smruDaqParameters.getChannelMask(iBoard) == 0) {
				// nothing to do for this particular board, so loop over ...
				smruDaqJNI.setSynchMode(iBoard, SmruDaqJNI.SMRU_VAL_STANDALONE);
				continue;
			}
			nUsed++; // need to keep count of this for master / slave settings. 

			boolean ok = prepareDaqCard(iBoard, fullReset);
			if (ok) {
				nPrepped ++;
			}
			else {
				continue;
			}

			if (!smruDaqJNI.setSampleRateIndex(iBoard, smruDaqParameters.sampleRateIndex)) {
				prepareErrors ++;
			}

			/*
			 * Set the master slave status. The last board that's used will 
			 * be the master. 
			 */
			int ret;
			if (totalUsed == 1) {
				ret = smruDaqJNI.setSynchMode(iBoard, SmruDaqJNI.SMRU_VAL_STANDALONE);
				terminalPrint(String.format("Setting board %d synch as SMRU_VAL_STANDALONE", iBoard), 1);
			}
			else if (nUsed == totalUsed) {
				ret = smruDaqJNI.setSynchMode(iBoard, SmruDaqJNI.SMRU_VAL_MASTER);
				terminalPrint(String.format("Setting board %d synch as SMRU_VAL_MASTER", iBoard), 1);
			}
			else {
				ret = smruDaqJNI.setSynchMode(iBoard, SmruDaqJNI.SMRU_VAL_SLAVE);
				terminalPrint(String.format("Setting board %d synch as SMRU_VAL_SLAVE", iBoard), 1);
			}

			if (!smruDaqJNI.setChannelMask(iBoard, smruDaqParameters.getChannelMask(iBoard))) {
				prepareErrors ++;
			}

			for (int i = 0; i < SmruDaqParameters.NCHANNELS; i++) {
				if (!smruDaqJNI.prepareChannel(iBoard, i, smruDaqParameters.getGainIndex(iBoard, i), 
						smruDaqParameters.getFilterIndex(iBoard, i))) {
					prepareErrors++;
				}
			}
		}

		return nPrepped;

	}

	boolean prepareDaqCard(int iBoard, boolean fullReset) {
		// devices are left closed, so will need to reopen them. 
		// of course, there is a vile lookup table, so... 
		smruDaqJNI.setLED(iBoard, SmruDaqJNI.GREEN_LED, 0);
		smruDaqJNI.setLED(iBoard, SmruDaqJNI.RED_LED, 0);
		int hardId = smruDaqJNI.getBoardOrder(iBoard);
		int prepOk = smruDaqJNI.prepareDevice(hardId, fullReset);
		terminalPrint("Opened daq card returned " + prepOk, 1);		
		long sn = smruDaqJNI.readDeviceSerialNumber(hardId); // good test to see if it's open. 
		terminalPrint("Opened daq card returned sn " + sn, 1);		
		return (sn != 0 && prepOk == SmruDaqJNI.SMRU_RET_OK);
	}

	public int toggleLED(int cardId, int led) {
		return smruDaqJNI.toggleLED(cardId, led);
	}

	public int getLED(int cardId, int led) {
		if (smruDaqJNI == null) {
			return -2;
		}
		return smruDaqJNI.getLED(cardId, led);
	}

	private volatile boolean keepRunning;
	private volatile boolean daqThreadRunning;

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		boolean ans = true;
		if (prepareErrors > 0) {
			return false;
		}
		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {

			if (smruDaqParameters.getChannelMask(iBoard) == 0) {
				continue;
			}
			ans &= smruDaqJNI.startSystem(iBoard);
		}

		if (ans) {
			Thread t = new Thread(new DaqThread());
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
			keepRunning = true;
			daqThreadRunning = true;
		}
		else {
			terminalPrint("Error starting one or more SAIL DAQ cards", 1);
		}

		return ans;
	}

	/**
	 * Runs in a separate thread during acquisition. 
	 */
	private void acquireData() {
		newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		double[] dcOffset = new double[PamConstants.MAX_CHANNELS];
		double dcOffsetScale = 10.;
		int dcOffsetCalls = 0;
		double dcTotal;
		short[] newData;
		int readSamples = 0;
		long dataMillis;
		//		int nChan = PamUtils.getNumChannels(this.smruDaqParameters.channelMask);
		int[] chans = PamUtils.getChannelArray(smruDaqParameters.channelMask);
		int[] boardChannels = new int[nDaqCards];
		for (int i = 0; i < nDaqCards; i++) {
			boardChannels[i] = PamUtils.getNumChannels(smruDaqParameters.getChannelMask(i));
		}
		/*
		 * Need to make some crazy list relating 0,1,2,3 channels to 
		 * board numbers and to channel numbers. 
		 */
		//		int[] boardIds = new int[nChan];
		//		int[] channelIds = new int[nChan];
		//		int iChan = 0; 
		//		int iBoard = 0;
		//		int chanIndex = 0;
		//		int boardIndex = 0;
		//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
		//			if ((smruDaqParameters.channelMask & (1<<i)) == 0) {
		//				continue;
		//			}
		//			iBoard = i/SmruDaqParameters.NCHANNELS;
		//			if (i%SmruDaqParameters.NCHANNELS == 0) {
		//				iChan = 0;
		//			}
		//			boardIds[chanIndex] = iBoard;
		//			channelIds[chanIndex] = iChan++;
		//		}

		int bytesPerSample = 2;
		long totalSamples = 0;
		RawDataUnit rawDataUnit;
		double[] rawData;
		boolean first = true;
		boolean needRestart = false;
		int iChan;
		// some flags on checks of incoming data rate
//		long recentSamples = 0;
//		long recentCheckTime = System.currentTimeMillis();
		int loopCount = 0;
		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {
			smruDaqJNI.setLED(iBoard, SmruDaqJNI.GREEN_LED, 1);
			smruDaqJNI.setLED(iBoard, SmruDaqJNI.RED_LED, 0);
		}
		
		while (keepRunning) {
			iChan = 0;
			dataMillis = daqControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
			if (isStalled()) {
				needRestart = true;
				for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {
					smruDaqJNI.setLED(iBoard, SmruDaqJNI.GREEN_LED, 0);
					smruDaqJNI.setLED(iBoard, SmruDaqJNI.RED_LED, 1);
				}
				// don't set this false or the shutdown doesn't work properly.
//				keepRunning = false;
				break;
			}
			loopCount++;
			for (int iBoard = 0; iBoard < nDaqCards && keepRunning; iBoard++) {
				if ((loopCount % 10000) == 0) {
//					toggleLED(iBoard, 0);
					smruDaqJNI.setLED(iBoard, SmruDaqJNI.GREEN_LED, 1);
				}
				for (int i = 0; i < boardChannels[iBoard] && keepRunning; i++) {
					newData = smruDaqJNI.readSamples(iBoard, i, wantedSamples);
					if (newData == null) {
						smruDaqJNI.setLED(iBoard, SmruDaqJNI.RED_LED, 1);
						System.out.println(String.format("Null data read from smruDaqJNI.readSamples board %d, chan %d, samples %d", 
								iBoard, i, wantedSamples));
//						System.out.println("Issue restart ...");
						needRestart = true;
//						keepRunning = false;
						break;
					}
					readSamples = newData.length;
					if (readSamples < wantedSamples) {
						System.out.println(String.format("Error reading board %d channel %d got %d samples of %d requested",
								iBoard, i, readSamples, wantedSamples));
					}
					dcTotal = 0;
					rawData = new double[readSamples];
					for (int s = 0; s < readSamples; s++) {
						rawData[s] = (newData[s]-dcOffset[iChan]) / 32768.;
						dcTotal += newData[s];
					}
					dcOffset[iChan] += (dcTotal/readSamples - dcOffset[iChan]) / dcOffsetScale;

					rawDataUnit = new RawDataUnit(dataMillis, 1 << iChan, totalSamples, readSamples);
					rawDataUnit.setRawData(rawData);
					newDataUnits.addNewData(rawDataUnit);
					first = false;
					iChan++;
				}
				
			}
			if (++dcOffsetCalls == 100) {
				dcOffsetScale = 100.; // after a bit, increase the time constant. 
			}
			


			totalSamples += readSamples;

		}
		daqThreadRunning = false;
		
		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {
			smruDaqJNI.setLED(iBoard, SmruDaqJNI.GREEN_LED, 0);
		}
		
		if (needRestart) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					PamController pamController = PamController.getInstance();
					pamController.pamStop();
					System.out.println("Problem with one or more SAIL DAQ Cards. "
							+ "Restart PAMGuard, check connections and try again." );
//					PamDialog.showWarning(daqControl.getGuiFrame(), daqControl.getUnitName(), "Problem with one or more SAIL DAQ Cards.\n"
//							+ "Restart PAMGuard, check connections and try again." );
					pamController.startLater();					
				}
			});
		}
	}

//	private long lastFakeStall = 0;
	private boolean isStalled() {
//		if (lastFakeStall == 0) {
//			lastFakeStall = System.currentTimeMillis();
//		}
//		long now = System.currentTimeMillis();
//		if (now-lastFakeStall > 10000) {
//			System.out.println("Random pretend stalled");
//			lastFakeStall = 0;
//			return true;
//		}
		return daqControl.getAcquisitionProcess().isStalled();
	}

	public static short getSample(byte[] buffer, int position) {
		//		return (short) (((buffer[position] & 0xff) << 8) | (buffer[position + 1] & 0xff));
		return (short) (((buffer[position+1] & 0xff) << 8) | (buffer[position] & 0xff));
	}

	@Override
	public double getChannelGain(int channel) {
		return smruDaqParameters.getChannelGain(channel);
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		if (!keepRunning) {
			/*
			 *  it was never running, so no need to do this. From the GUI this isn't ever a problem 
			 *  but PAMDog can call stop a little over zealously, and there seems to be a problem
			 *  with calling this when it's not running bunging up the cards. So basically without
			 *  this PAMDog won't run properly with PAMDog 
			 */
			return;
		}
		keepRunning = false;
		try {
			while(daqThreadRunning) {
				Thread.sleep(2);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {

			if (smruDaqParameters.getChannelMask(iBoard) == 0) {
				continue;
			}
			terminalPrint("In stopSystem board " + iBoard, 1);
			
			smruDaqJNI.stopSystem(iBoard);
			smruDaqJNI.closeCard(iBoard);
			//			smruDaqJNI.resetCard(iBoard);
		}
	}

	@Override
	public void daqHasEnded() {
		for (int iBoard = 0; iBoard < nDaqCards; iBoard++) {

			if (smruDaqParameters.getChannelMask(iBoard) == 0) {
				continue;
			}
			terminalPrint("In daqHasEnded systemStopped board " + iBoard, 1);
			smruDaqJNI.systemStopped(iBoard);
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return smruDaqParameters;
	}

	@Override
	public long getSettingsVersion() {
		return SmruDaqParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
//		return daqControl.getUnitName() + " SAIL Daq Card";
		return daqControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return daqCardName;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		smruDaqParameters = ((SmruDaqParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	class DaqThread implements Runnable {

		@Override
		public void run() {
			acquireData();
		}

	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPlaybackSystem(soundPlayback.PlaybackControl, Acquisition.DaqSystem)
	 */
	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl,
			DaqSystem daqSystem) {
		return playbackControl.getFilePlayback();
	}

	/**
	 * @return the smruDaqJNI
	 */
	public SmruDaqJNI getSmruDaqJNI() {
		return smruDaqJNI;
	}

	public SmruDaqParameters getSmruDaqParameters() {
		return smruDaqParameters;
	}


}
