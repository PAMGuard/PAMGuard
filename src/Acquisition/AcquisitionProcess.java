package Acquisition;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import Acquisition.gpstiming.PPSDetector;
import Acquisition.gpstiming.PPSParameters;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import Array.Preamplifier;
import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IirfFilter;
import PamController.DataInputStore;
import PamController.InputStoreInfo;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.fileprocessing.ReprocessManager;
import PamController.fileprocessing.StoreStatus;
import PamController.status.BaseProcessCheck;
import PamController.status.ProcessCheck;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.time.ntp.PamNTPTime;
import PamUtils.time.ntp.PamNTPTimeException;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewLoadObserver;

/**
 * Data acquisition process for all types of input device. 
 * This arranges the output data block and starts and stops the 
 * device in the detected DaqSystem. Each DaqSystem should 
 * operate a different thread to read the device / file and add its
 * data to the volatile Vector newDataUnits. AcquisitonProcess will 
 * poll newDataUnits on a timer and when new data is found, put that 
 * data into PamDataUnits and PamRawDataBlocks to be sent out for 
 * processing.
 * <p>
 * @author Doug Gillespie
 * @see Acquisition.DaqSystem
 * @see PamguardMVC.PamRawDataBlock
 * @see PamguardMVC.PamDataUnit
 *
 */
public class AcquisitionProcess extends PamProcess implements DataInputStore {
	
	public static final int LASTDATA = 2; // don't use zero since need to see if no notification has been received. 
	
	public static final int FIRSTDATA = 1;

	AcquisitionControl acquisitionControl;

	private volatile DaqSystem runningSystem = null;

	protected PamRawDataBlock rawDataBlock;

	private PamDataBlock<DaqStatusDataUnit> daqStatusDataBlock;

	AcquisitionProcess acquisitionProcess;

	private int dataBlockLength = 0;

//	long[] totalSamples;

	private volatile boolean keepRunning = false;

	private volatile boolean bufferOverflow = false;

	private Timer restartTimer;
	
	private Filter sampleRateErrorFilter;
	
	private double totalExtraSamples;

	private DaqStatusDataUnit previousDaqStatus = null;

	private long millisecondSampleOffset;
	
	private AudioDataQueue newDataQueue = new AudioDataQueue();

	private final double sqrt2 = Math.sqrt(2.0);

	private Object runingSynchObject = new Object();
	
	private PPSDetector ppsDetector;
	
	private DCFilter dcFilter;

	protected AcquisitionProcess(AcquisitionControl acquisitionControl) {

		super(acquisitionControl, null);

		acquisitionProcess = this;

		this.acquisitionControl = acquisitionControl;
		
		ppsDetector = new PPSDetector(this);

		String name = String.format("Raw input data from %s", acquisitionControl.getUnitName());

		//addOutputDataBlock(rawDataBlock = new PamRawDataBlock(name, this,
		//		PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels), 
		//		acquisitionControl.acquisitionParameters.sampleRate));

		addOutputDataBlock(rawDataBlock = new PamRawDataBlock(name, this, //Xiao Yan Deng
				PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels,acquisitionControl.acquisitionParameters.getHardwareChannelList()),
				acquisitionControl.acquisitionParameters.sampleRate));


		daqStatusDataBlock = new PamDataBlock<DaqStatusDataUnit>(DaqStatusDataUnit.class, acquisitionControl.getUnitName(),
				this, 0);
//		daqStatusDataBlock.
		addOutputDataBlock(daqStatusDataBlock);
		daqStatusDataBlock.SetLogging(new AcquisitionLogging(daqStatusDataBlock, acquisitionControl));
		
		/**
		 * We really don't wand the binary data source set for normal ops since it stops the data getting
		 * written to the database by default. When using certain Network receiver settings, they use binary 
		 * type data, so do need it. this will therefore be configured from the network receiver when required.  
		 */
//			daqStatusDataBlock.setBinaryDataSource(new DaqStatusBinaryStore(daqStatusDataBlock, acquisitionControl));
			AbstractScrollManager.getScrollManager().addToSpecialDatablock(daqStatusDataBlock);
		daqStatusDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);

		setupDataBlock();

		restartTimer = new Timer(200, new RestartTimerFunction());
		restartTimer.setRepeats(false);
//		stallCheckTimer = new Timer(60000, new StallCheckAction());

		bufferTimer = new Timer(1000, new BufferTimerTest());
		
		ProcessCheck pp = new BaseProcessCheck(this, null, 0, 10);
		setProcessCheck(pp);
		
		/**
		 * Make a filter to low pass filter the sample rate errors. these 
		 * will eventually get used to update / fix estimated RawDataunit milliseconds
		 * times based on the total sample offset compared to UTC. 
		 */
		FilterParams filterParams = new FilterParams();
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.lowPassFreq = 0.1f;
		filterParams.filterType = FilterType.BUTTERWORTH;
		filterParams.filterOrder = 4;
		sampleRateErrorFilter = new IirfFilter(1, 1, filterParams);

	}

	protected AcquisitionProcess(AcquisitionControl acquisitionControl, boolean isSimulator) {

		super(acquisitionControl, null);

		acquisitionProcess = this;

		this.acquisitionControl = acquisitionControl;

	}

	/**
	 * called when acquisition parameters change.
	 *
	 */
	public void setupDataBlock() {
		//rawDataBlock.setChannelMap(PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels));
		//		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels,
		//				                                            acquisitionControl.acquisitionParameters.getHardwareChannelList()));//Xiao Yan Deng
		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels));
		setSampleRate(acquisitionControl.acquisitionParameters.sampleRate, true);
	}



	private boolean systemPrepared;

	/**
	 * Time of last DAQ check in milliseconds
	 */
	//	private long daqCheckTime;
	/**
	 * Interval between daq checks in milliseconds
	 */
	//	private long daqCheckInterval = 60 * 1000;
	@Override
	public void pamStart() {
		// called by PamController. Don't actually start if 
		// we're in network receive mode. 

		if (systemPrepared == false) return;

		//		before starting, clear all old data
		rawDataBlock.clearAll();

		newDataQueue.clearList();
		
		sampleRateErrorFilter.prepareFilter();
		totalExtraSamples = 0;
		millisecondSampleOffset = 0;
		
		if (runningSystem == null) {
			return;
		}
		
		boolean netRX = PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER;
		if (!netRX) {
			if (runningSystem.startSystem(acquisitionControl) == false) return;
		}
		// Log a data unit asap to get the start time
		long daqStartedTime = PamCalendar.getTimeInMillis();

		// let all the observers know that the DAQ has started
		sendSourceNotification(FIRSTDATA, null);

		/**
		 * Now sstart the thread which will pull data back from the daq system
		 */
		collectDataThread = new Thread(new WaitForData());
		collectDataThread.setPriority(Thread.MAX_PRIORITY);

		acquisitionControl.daqMenuEnabler.enableItems(false);
		acquisitionControl.fillStatusBarText();

		bufferTimer.start();
		
//		stallCheckTimer.start();

		/**
		 * Now log the data unit.
		 */
		DaqStatusDataUnit daqStatusDataUnit = new DaqStatusDataUnit(daqStartedTime, daqStartedTime, daqStartedTime,
				0, null, "Start", "", acquisitionControl.acquisitionParameters, runningSystem.getSystemName(), 0, 0);
		lastStatusTime = daqStartedTime;
		addServerTime(daqStatusDataUnit);
		daqStatusDataBlock.addPamData(daqStatusDataUnit);
		previousDaqStatus = daqStatusDataUnit;
		
		/*
		 * All systems work in much the same way - set up a timer to look for new data which is
		 * put there by a separate thread that gets the data from it's source.
		 * 
		 */
		
		bufferOverflow = false;
		//		daqCheckTime = PamCalendar.getTimeInMillis();
		if (!netRX) {
			Timer t = new Timer(1, new ReallyStart());
			t.setRepeats(false);
			t.start();
		}
	}
	
	private boolean addServerTime(DaqStatusDataUnit daqStatusDataUnit) {
		if (runningSystem.isRealTime() == false) {
			return false;
		}
//		PamNTPTime pamNTPTime = PamNTPTime.getInstance();
//		try {
//			long serverTime = pamNTPTime.getNTPTime();
//			daqStatusDataUnit.setServerTime(serverTime);
//			return true;
//		} catch (PamNTPTimeException e) {
//			return false;
//		}
		return false;
	}

	Thread collectDataThread;
	class ReallyStart implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			collectDataThread.start();
		}

	}

	@Override
	public void pamStop() {
		// called by PamController.
		// stop the running system - not the selected system since
		// this may have changed
		restartTimer.stop();
//		stallCheckTimer.stop();
		pamStop("");


	}

//	private class StallCheckAction implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			Boolean s = isStalled();
////			System.out.printf("%s Stall state = %s\n", PamCalendar.formatDateTime(System.currentTimeMillis()), s.toString());
//		}
//		
//	}
	
	private long simpleSamplesToMilliseconds(long samples) {
		return (long) (samples * 1000. / sampleRate) + PamCalendar.getSessionStartTime();
	}
	
	/**
	 * Periodic logging of ADC status into database. 
	 */
	private void logRunningStatus() {
		if (runningSystem == null) return;
		long samplesRead = newDataQueue.getSamplesIn(0);
		double duration = (double) samplesRead / getSampleRate();
		double clockError = checkClockSpeed(samplesRead, 1);
		long adcMillis = this.absSamplesToMilliseconds(samplesRead);
		long simpleTime= simpleSamplesToMilliseconds(samplesRead);
		DaqStatusDataUnit ds = new DaqStatusDataUnit(PamCalendar.getTimeInMillis(), adcMillis, simpleTime,
				samplesRead, null, "Continue", "", 
				acquisitionControl.acquisitionParameters, runningSystem.getSystemName(), duration, clockError);
		addServerTime(ds);
		if (runningSystem.isRealTime()) {
			long pcTime = PamCalendar.getTimeInMillis(); // time hopefully corrected from NTP or GPs. 
			millisecondSampleOffset = (long) sampleRateErrorFilter.runFilter(pcTime-simpleTime);
		}
//		System.out.printf("Current ADC clock correction at %s %d millis\n" , PamCalendar.formatDateTime(pcTime), millisecondSampleOffset);
		
//		System.out.printf("Sample rate estimated at %s, %dMSamples = %7.2fHz, filtered err %3.1fHz, TotExtraSamples = %3.1f, offest %d mills\n", 
//		PamCalendar.formatDateTime(ds.getTimeMilliseconds()), samplesRead/1000000, estSampleRate, filteredEstSampleRateError, totalExtraSamples, millisecondSampleOffset);
//		if (previousDaqStatus != null) {
////			Double trueSampleRate = ds.calculateTrueSampleRate(previousDaqStatus);
//			double estSampleRate = 1000 * (double) (samplesRead - previousDaqStatus.getSamples()) / (double) (ds.getTimeMilliseconds() - previousDaqStatus.getTimeMilliseconds());
//			double filteredEstSampleRateError = sampleRateErrorFilter.runFilter(estSampleRate-getSampleRate());
//			double extExtraSamples =  (ds.getTimeMilliseconds() - previousDaqStatus.getTimeMilliseconds()) * filteredEstSampleRateError / 1000.;
//			totalExtraSamples += extExtraSamples;
//			millisecondSampleOffset = (long) (totalExtraSamples / getSampleRate() * 1000.);
//			/*
//			 * Extra samples +ve means that more samples have arrived than we expected. Therefore the estimate of the 
//			 * UTC of the next RawDataunit is going to be too high, so we'll need to subtract off the millisecondSampleOffset
//			 * when we create a raw data unit.  
//			 */
////			if (trueSampleRate != null) {
//				System.out.printf("Sample rate estimated at %s, %dMSamples = %7.2fHz, filtered err %3.1fHz, TotExtraSamples = %3.1f, offest %d mills\n", 
//						PamCalendar.formatDateTime(ds.getTimeMilliseconds()), samplesRead/1000000, estSampleRate, filteredEstSampleRateError, totalExtraSamples, millisecondSampleOffset);
//				
////			}
//		}
		daqStatusDataBlock.addPamData(ds);
		previousDaqStatus = ds;
	}

	public void pamStop(String reason) {
		if (runningSystem == null) return;
		if (reason == null) reason = new String("");
		long samplesRead = newDataQueue.getSamplesIn(0);
		double duration = (double) samplesRead / getSampleRate();
		double clockError = checkClockSpeed(samplesRead, 2);
		long adcMillis = this.absSamplesToMilliseconds(samplesRead);
		long simpleMillis = this.simpleSamplesToMilliseconds(samplesRead);
		DaqStatusDataUnit ds = new DaqStatusDataUnit(PamCalendar.getTimeInMillis(), adcMillis, simpleMillis,
				samplesRead, null, "Stop", reason, 
				acquisitionControl.acquisitionParameters, runningSystem.getSystemName(), duration, clockError);
		addServerTime(ds);
		daqStatusDataBlock.addPamData(ds);
		
		runningSystem.stopSystem(acquisitionControl);

		keepRunning = false;

		bufferTimer.stop();

		acquisitionStopped();

	}

	public void recordPPSOffset(long timeNow, long edgeSample, long gpsUTC, Long serverTime) {
		long adcMillis = absSamplesToMilliseconds(edgeSample);
		long simpleMillis = simpleSamplesToMilliseconds(edgeSample);
		double duration = (double) edgeSample / getSampleRate();
		double clockError = checkClockSpeed(edgeSample, 0);
		DaqStatusDataUnit ds = new DaqStatusDataUnit(timeNow, adcMillis, simpleMillis,
				edgeSample, gpsUTC, "GPSPPS", "", 
				 acquisitionControl.acquisitionParameters, runningSystem.getSystemName(), duration, clockError);
		ds.setServerTime(serverTime);

		daqStatusDataBlock.addPamData(ds);
		
	}

	public void acquisitionStopped() {
		/* 
		 * can get called by a DaqSystem thread just as it exits to 
		 * say that DAQ has stopped. Only needs to be implemented for things
		 * like files which will stop themselves. Can also be implemented for 
		 * other devices which might stop accidentally (e.g. UDP sources)
		 */

		DaqSystem selectedSystem = runningSystem;

		if (collectDataThread == null) {
			return;
		}
		//		daqTimer.stop();
		while (collectDataThread.isAlive()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// call acquireData one last time to make sure that 
		// all data have been flushed from the buffer. 
		if (bufferOverflow == false) {
			acquireData();
		}
		else {
			clearData();
		}

		acquisitionControl.daqMenuEnabler.enableItems(true);

		/*
		 * runningSystem needs to be set null here since the call to PamController.PamStop()
		 * will call back to pamStop and we'll get an infinite loop !
		 * Synch on runingSynchObject to stop crash during shut down of system. 
		 */
		synchronized(runingSynchObject) {
			runningSystem = null;
		}
		//		System.out.println("Set running system null");

		acquisitionControl.fillStatusBarText();

		// let all observers know that the daq has stopped
		// Note that this is not in the collectDataThread thread; by the time we
		// reach this part of the code, that thread has already emptied out and
		// died.
		// Note also that we need to send this out BEFORE we call pamStop, because
		// this call will alert the ThreadedObservers to empty out their buffers.
		// PamController.pamStop will wait for that to happen, so if we call pamStop
		// first it will hang.
		sendSourceNotification(LASTDATA, null);
		
		// stop all controlled units
		// 2021-05-14 removed - not sure why this is here, and it's just causing PamController.pamStop to get called twice
//		acquisitionControl.pamController.pamStop();

	}
	
	/**
	 * Called from AcquisitionControl when all observer thread data queues
	 * have been emptied and processing has stopped
	 */
	protected void pamHasStopped() {
		if (runningSystem == null){
			runningSystem = acquisitionControl.findDaqSystem(null);
		}
		if (runningSystem != null) {
			runningSystem.daqHasEnded();
		}
	}
	
	/**
	 * Let all observers of the raw data know that the daq status has changed.  Right now,
	 * this is intended to let everyone know that the Daq has started or stopped, so that
	 * the various threads can clean themselves up.
	 * 
	 * @param type the type of change (see global fields at the top of this class)
	 * @param object (null for now, but added in case we need to pass something later)
	 */
	public void sendSourceNotification(int type, Object object) {
		int num = rawDataBlock.countObservers();
		for (int i=0; i<rawDataBlock.countObservers(); i++) {
			rawDataBlock.getPamObserver(i).receiveSourceNotification(type, object);
		}
	}
	
	@Override
	public boolean prepareProcessOK() {
		super.prepareProcessOK();
		return systemPrepared;
	}

	@Override
	// prepares data acquisition
	public void prepareProcess() {

		systemPrepared = false;

		lastStallCheckTime = 0;
		lastStallCheckSamples = 0;
		lastStallState = false;
		
		if (acquisitionControl.acquisitionParameters.subtractDC) {
			dcFilter = new DCFilter(acquisitionControl.acquisitionParameters.sampleRate, 
					acquisitionControl.acquisitionParameters.dcTimeConstant, PamConstants.MAX_CHANNELS);
		}
		else {
			dcFilter = null;
		}

		setSampleRate(acquisitionControl.acquisitionParameters.sampleRate, true);

		super.prepareProcess();
		
		ppsDetector.prepare(acquisitionControl.acquisitionParameters);

		if (runningSystem != null) {
			pamStop();
		}
		dataBlockLength = -1;
		
		runningSystem = acquisitionControl.findDaqSystem(null);
		if (runningSystem == null) {
			System.out.printf("Unable to find daq system %s\n", acquisitionControl.acquisitionParameters.daqSystemType);
			return;
		}
		
		systemPrepared = runningSystem.prepareSystem(acquisitionControl);

	}


	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		acquisitionControl.acquisitionParameters.sampleRate = sampleRate;
		//		System.out.println("Acquisition set sample rate to " + sampleRate);
		super.setSampleRate(sampleRate, notify);

	}

	public void setNumChannels(int numChannels) {  //Xiao Yan Deng commented
		acquisitionControl.acquisitionParameters.nChannels = numChannels;
		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(numChannels));
	}

	/**
	 * Set up channels when using a channel list - note
	 * that hardware channels are no longer passed through the system
	 * so software channels are now used throughout. 
	 * @param numChannels
	 * @param channelList
	 */
	public void setNumChannels(int numChannels, int[] channelList) { //Xiao Yan Deng
		acquisitionControl.acquisitionParameters.nChannels = numChannels;
		acquisitionControl.acquisitionParameters.setChannelList(channelList);
		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(numChannels));
		//		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(numChannels, channelList)); 
	}

	/*
	 * The timer looks for new data in the vector newDataUnits.
	 * Whenever new data is there it is taken out and passed on
	 * to the rest of Pamguard by adding it to the datablock.
	 */
	//	Timer daqTimer = new Timer(5, new ActionListener() {
	//		public void actionPerformed(ActionEvent evt) {
	////			runTimerActions();
	//		}
	//	});

	Timer bufferTimer;

	class BufferTimerTest implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			acquisitionControl.fillStatusBarText();

			if (needRestart()) {
				bufferOverflow = true;
				restartTimer.start();
			}
		}

	}

	class WaitForData implements Runnable {

		@Override
		public void run() {
			keepRunning = true;
			while (keepRunning) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (acquireData() == false) {
					break;
				}
			}
		}

	}

	int trials = 0;
	int counts = 0;
	double maxLevel;
	long levelSamples;
	double[] rawData;

	private long lastStatusTime;
	private long statusInterval = 60000;

	public void streamClosed() {
		//		acquisitionStopped();
	}
	public void streamEnded() {
		//		acquisitionStopped();
	}
	public void streamOpen() {

	}
	public void streamPaused() {

	}


	/**
	 * Used to restart after a buffer overflow. 
	 * @author Doug Gillespie
	 *
	 */
	class RestartTimerFunction implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {

			System.out.println("PAMGUARD cannot process data at the required rate and is restarting");

			restartTimer.stop();

			PamController.getInstance().pamStop();

			PamController.getInstance().pamStart(false);

		}

	}

	private boolean acquireData() {
		if (runningSystem == null) {
			return false;
		}
		RawDataUnit newDataUnit, threadDataUnit;

		int readCount = 0;
		while (newDataQueue.hasData()) {
//			System.out.println("size:"+newDataUnits.size());
			threadDataUnit = newDataQueue.removeOldest();

			int channel = PamUtils.getSingleChannel(threadDataUnit.getChannelBitmap());
//			long unitMillis = absSamplesToMilliseconds(threadDataUnit.getStartSample()) - millisecondSampleOffset;
			long unitMillis = (long) simpleSamplesToMilliseconds(threadDataUnit.getStartSample()) + millisecondSampleOffset;
			newDataUnit = new RawDataUnit(unitMillis, 
					threadDataUnit.getChannelBitmap(), threadDataUnit.getStartSample(),
					threadDataUnit.getSampleDuration());
			if (dcFilter != null) {
				dcFilter.filterData(channel, threadDataUnit.getRawData());
			}
			newDataUnit.setRawData(threadDataUnit.getRawData(), true);
			acquisitionControl.setStatusBarLevel(newDataUnit.getMeasuredAmplitude());
			// can also here convert the measured amplitude (which was
			// calculated in the call to setRawData, to dB.
			newDataUnit.setCalculatedAmlitudeDB(rawAmplitude2dB(newDataUnit.getMeasuredAmplitude(),
					PamUtils.getSingleChannel(threadDataUnit.getChannelBitmap()), false));
			
			PPSParameters ppsParams = acquisitionControl.acquisitionParameters.getPpsParameters();
			if (ppsParams.useGpsPPS & newDataUnit.getChannelBitmap() == 1<<ppsParams.gpsPPSChannel){
				ppsDetector.newData(newDataUnit);
			}
			
			addData(null, newDataUnit);
			rawData = newDataUnit.getRawData();
			dataBlockLength = rawData.length;
			for (int i = 0; i < rawData.length; i++) {
				maxLevel = Math.max(maxLevel, Math.abs(rawData[i]));
			}
			levelSamples += rawData.length;
			if (bufferOverflow) {
				break;
			}
			
			if (threadDataUnit.getChannelBitmap() == 1<<(acquisitionControl.getAcquisitionParameters().nChannels-1) &&
					threadDataUnit.getTimeMilliseconds() - lastStatusTime > statusInterval) {
				logRunningStatus();
				lastStatusTime += statusInterval;
			}
		}
		return true;
	}

	private void clearData() {
		newDataQueue.clearList();
	}

	//	private void streamRunning(boolean finalFlush) {
	//		if (runningSystem == null) return;
	//		if (runningSystem.isRealTime()) {
	//			/*
	//			 * Do a test to see if we're getting too far behind. For now hard wire a
	//			 * buffer with a 10s maximum
	//			 */
	//			if (needRestart() && finalFlush == false) {
	//				
	//				System.out.println(PamCalendar.formatDateTime(System.currentTimeMillis()) + 
	//						" : Emergency sound system restart due to buffer overflow");
	//				pamStop("Buffer overflow in sound system");
	//				
	//				newDataUnits.clear();
	//				
	//				acquisitionStopped();
	//				
	//				restartTimer.start();
	//				
	//				return;
	//			}
	//			
	//			long now = PamCalendar.getTimeInMillis();
	//			if (now - daqCheckTime >= daqCheckInterval) {
	//				double duration = (double) totalSamples[0] / getSampleRate();
	//				double clockError = checkClockSpeed(totalSamples[0], 1);
	//				DaqStatusDataUnit ds = new DaqStatusDataUnit(PamCalendar.getTimeInMillis(), "Continue", "Check", 
	//						runningSystem.getSystemName(), getSampleRate(), acquisitionControl.acquisitionParameters.nChannels,
	//						acquisitionControl.acquisitionParameters.voltsPeak2Peak, duration, clockError);
	//				daqStatusDataBlock.addPamData(ds);
	//				daqCheckTime = now;
	//			}
	//			
	//			/*
	//			 * The blocks should be in pairs, so there should generally 
	//			 * be two blocks there every time this gets called. Adjust timing
	//			 * automatically to deal with just about any data rate. Start at a low
	//			 * value though since file reading only adds blocks if there are none
	//			 * there - so would never reduce the delay ! 
	//			 * 
	//			 * Don't do this if it isn't a real time process since we want to 
	//			 * keep going as fast as possible
	//			 */
	////			trials++;
	////			counts += newDataUnits.size();
	////			if (trials == 15 || counts >= 40) {
	////				if (trials > counts * 3) {
	////					daqTimer.setDelay(Math.max(10,daqTimer.getDelay() * 5 / 4));
	////					System.out.println("Increasing timer delay to " + daqTimer.getDelay() + " ms");					
	////				}
	////				else if (counts > trials * 2) {
	////					daqTimer.setDelay(Math.max(1,daqTimer.getDelay() * 2 / 3));
	////					System.out.println("Reducing timer delay to " + daqTimer.getDelay() + " ms");		
	////				}
	////				trials = counts = 0;
	////			}
	//		}
	//		
	//		RawDataUnit newDataUnit, threadDataUnit;
	//		
	//		int readCount = 0;
	//		while (!newDataUnits.isEmpty()) {
	//
	//			threadDataUnit = newDataUnits.remove(0);
	//			
	//			int channel = PamUtils.getSingleChannel(threadDataUnit.getChannelBitmap());
	//			newDataUnit = new RawDataUnit(absSamplesToMilliseconds(threadDataUnit.getStartSample()), 
	//					threadDataUnit.getChannelBitmap(), threadDataUnit.getStartSample(),
	//					threadDataUnit.getDuration());
	//			newDataUnit.setRawData(threadDataUnit.getRawData(), true);
	//			// can also here convert the measured amplitude (which was
	//			// calculated in the call to setRawData, to dB.
	//			newDataUnit.setCalculatedAmlitudeDB(rawAmplitude2dB(newDataUnit.getMeasuredAmplitude(),
	//					PamUtils.getSingleChannel(threadDataUnit.getChannelBitmap()), false));
	//			update(null, newDataUnit);
	//			rawData = newDataUnit.getRawData();
	//			dataBlockLength = rawData.length;
	//			totalSamples[channel] += dataBlockLength;
	//			for (int i = 0; i < rawData.length; i++) {
	//				maxLevel = Math.max(maxLevel, Math.abs(rawData[i]));
	//			}
	//			levelSamples += rawData.length;
	//			
	//
	//			// about every 5 seconds, check the buffer isn't filling
	//			if (newDataUnit.getAbsBlockIndex() % (50 * acquisitionControl.acquisitionParameters.nChannels) == 0) {
	//				double buffer = getBufferEstimate(newDataUnit.getStartSample() + newDataUnit.getDuration());
	//				if (buffer > 3) {
	//					System.out.println(PamCalendar.formatDateTime(System.currentTimeMillis()) + 
	//					" : Emergency sound system restart due to Buffer overflow type 2");
	//					pamStop("Type 2 Buffer overflow in sound system");
	//
	//					newDataUnits.clear();
	//
	//					acquisitionStopped();
	//
	//					restartTimer.start();
	//
	//					return;
	//				}
	//			}
	//				
	//			// about every minute, or every 1200 blocks, check the timing
	//			if ((newDataUnit.getAbsBlockIndex()+1) % (600 * acquisitionControl.acquisitionParameters.nChannels) == 0) {
	//				checkClockSpeed(newDataUnit.getStartSample() + newDataUnit.getDuration(), 1);
	//			}
	//			// only ever stay in here for 10 reads - if it's getting behind, tough !
	//			// need to keep the GUI going whatever.
	//			if (++readCount >= acquisitionControl.acquisitionParameters.nChannels * 4 && finalFlush == false) {
	//				break;
	//			}
	//			
	//		}
	//		if (levelSamples >= sampleRate * acquisitionControl.acquisitionParameters.nChannels * 2) {
	//			acquisitionControl.setStatusBarLevel(maxLevel);
	//			acquisitionControl.fillStatusBarText();
	//			levelSamples = 0;
	//			maxLevel = 0;
	//		}
	//	}

	/**
	 * Check and optionally display clock speed error
	 * @param totalSamples total samples acquired
	 * @param print 0 = never, 1 = if the error is > .2%, 2 always
	 * @return % clock speed error
	 */
	private double checkClockSpeed(long totalSamples, int print) {
		double clockSeconds = (PamCalendar.getTimeInMillis() - PamCalendar.getSessionStartTime()) / 1000.;
		double sampleSeconds = (double) totalSamples / getSampleRate();
		if (clockSeconds == 0) {
			return 0;
		}
		double soundCardError = (sampleSeconds - clockSeconds) / clockSeconds * 100;
		int missingSamples = (int) ((sampleSeconds - clockSeconds) * getSampleRate());
//		if (print >= 2 || (print >= 1 && shouldPrintSoundCardError(soundCardError))) {
//			System.out.println(String.format("%s at %3.2f%% PC clock speed after %d seconds (about %3.1f seconds or %d samples)", 
//					runningSystem.getSystemName(), soundCardError, (int)clockSeconds, (sampleSeconds - clockSeconds), missingSamples));
//		}
		return soundCardError;
	}

	private double lastError = 0;
	private boolean shouldPrintSoundCardError(double error) {
		if (lastError == error) {
			return false;
		}
		double change = Math.abs(error / (error - lastError));

		lastError = error;
		return (Math.abs(error) > .2 && change > 0.2);
	}

	//	private double getBufferEstimate(long totalSamples) {
	//		double clockSeconds = (PamCalendar.getTimeInMillis() - PamCalendar.getSessionStartTime()) / 1000.;
	//		double sampleSeconds = (double) totalSamples / sampleRate;
	//		return clockSeconds - sampleSeconds;
	//	}

	public double getBufferSeconds()
	{
//		if (newDataUnits == null) return 0;
//		if (dataBlockLength <= 0) return 0;
//		double blocksPerSecond = getSampleRate() / dataBlockLength * 
//		acquisitionControl.acquisitionParameters.nChannels;
//		return newDataUnits.size() / blocksPerSecond;
		return (double) newDataQueue.getQueuedSamples(0) / getSampleRate();
				
	}

	/**
	 * 
	 * @return the maximum number of seconds of data which can be buffered. 
	 * This used to be fixed at 3, but now that individual raw data blocks contain >> 1s
	 * of data for low frequency DAQ, this can be exceeded in a single
	 * data unit, which causes continual resets. 
	 */
	public double getMaxBufferSeconds() {
		/**
		 * return a default of 3s for standard 1/10s data units.
		 */
		int duSamples = runningSystem.getDataUnitSamples();
		return Math.max(duSamples / getSampleRate() * 30, 3.);
	}

	public boolean needRestart() {
		if (getBufferSeconds() > getMaxBufferSeconds()) {
			System.out.printf("Restart because %ss buffer > %ss maximum\n", getBufferSeconds(), getMaxBufferSeconds());
			return true;
		}
		return false; 
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		rawDataBlock.addPamData((RawDataUnit)arg);
		rawDataBlock.masterClockUpdate(PamCalendar.getTimeInMillis(), ((RawDataUnit)arg).getStartSample());
	}

//	public List<RawDataUnit> getNewDataUnits() {
//		return newDataUnits;
//	}
	public AudioDataQueue getNewDataQueue() {
		return newDataQueue;
	}

	public DaqSystem getRunningSystem() {
		return runningSystem;
	}

	/**
	 * Convert a raw amplitude to dB re 1 micropascal based on
	 * calibration information held in the AcquisitionController
	 * 
	 * @param rawAmplitude raw amplitude (should be -1 < rawAmplitude < 1)
	 * @return amplitude in dB re 1 uPa.
	 */
	public double rawAmplitude2dB(double rawAmplitude, int channel, boolean fast){

		channel = checkSingleChannel(channel);
		
		double constantTerm;
		if (fast && fixedAmplitudeConstantTerm[channel] != 0) {
			constantTerm = fixedAmplitudeConstantTerm[channel];
		}
		else {
			constantTerm = getAmplitudeConstantTerm(channel); 
		}

		double vp2p = getPeak2PeakVoltage(channel);

		/*
		 * Need an extra divide by 2 in here since the standard scaling of PAMGUARD
		 * data is -1 to +1, so data really needed to be scaled against half
		 * the peak to peak voltage. 
		 */
		double dB = 20 * Math.log10(rawAmplitude * vp2p / 2) - constantTerm;

		// if the answer is -Infinity or Infinity or NaN, just set it to 0
		if (!Double.isFinite(dB)) {
			dB = 0;
		}
		return dB;
	}

	/**
	 * Check it's a single channel and not a channel map. 
	 * This fundamentally messed up the amplitude calculations when the
	 * channels were > 2 since it was only taking the number of the lowest set 
	 * bit. So if a genuine channel was sent, rather than a channel map with a 
	 * single set channel, it messed up. Have made this function redundant
	 * and we need to be 100% sure that all amplitude calculations are sent a 
	 * channel number not a bitmap with a single set channel.  
	 * @param channel
	 * @return single channel if it seemed to be a bitmap. 
	 */
	private int checkSingleChannel(int channel) {
		return channel;
//		int bitCount = PamUtils.getNumChannels(channel);
//		if (bitCount > 1 || channel > 32) {
//			channel = PamUtils.getLowestChannel(channel);
//		}
//		return channel;
	}

	/**
	 * Some devices may be setting this per channel.
	 * @param swChannel software channel number
	 * @return peak to peak voltage range. 
	 */
	public double getPeak2PeakVoltage(int swChannel) {

		synchronized(runingSynchObject) {
			if (runningSystem == null) {
				return acquisitionControl.acquisitionParameters.voltsPeak2Peak;
			}
			double vpv = runningSystem.getPeak2PeakVoltage(swChannel);
			if (vpv < 0) {
				return acquisitionControl.acquisitionParameters.voltsPeak2Peak;
			}
			return vpv;
		}
	}

	/**
	 * A Constant used for fast amplitude calculations when things
	 * like preamp gain will remain constant. Contains a constant
	 * term in the SPL calculations bases on preamp gains and 
	 * hdrophone sensitivities. 
	 * Changes to be channel specific since with multi threading it goes horribly 
	 * wrong if different channels have different sensitivities. 
	 */
	private double[] fixedAmplitudeConstantTerm = new double[PamConstants.MAX_CHANNELS];
	
	private DaqSystem ampSystem;
	/**
	 * Gets the fixedAmplitudeConstantTerm based on channel and hydrophone This is 
	 * the hydrophone sensitivity + all gains + ADC sensitivity in counts / volt. 
	 * @param channel = single software channel
	 * @return constant term for amplitude calculations
	 */
	private double getAmplitudeConstantTerm(int channel) {
		channel = checkSingleChannel(channel);
		// need to fish around a bit working out which hydrophone it is, etc.
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int hydrophoneChannel = acquisitionControl.getChannelHydrophone(channel);
		if (hydrophoneChannel < 0) hydrophoneChannel = 0;
//		Hydrophone hydrophone = array.getHydrophone(hydrophoneChannel);
		double hSens = array.getHydrophoneSensitivityAndGain(hydrophoneChannel);
				
		Preamplifier preamp = acquisitionControl.acquisitionParameters.preamplifier;
		if (getRunningSystem() != null) {
			ampSystem = getRunningSystem();
		}
		if (ampSystem == null) {
			ampSystem = acquisitionControl.findDaqSystem(null);
		}
		double xtra = 0;
		if (ampSystem != null) {
			xtra = ampSystem.getChannelGain(channel); 
		}
		return (hSens + preamp.getGain() + xtra);
	}
	
	/**
	 * Prepares for fast amplitude calculations
	 * @param channel number i.e. 0 - 31, NOT a bitmap. 
	 */
	public double prepareFastAmplitudeCalculation(int channel) {
		channel = checkSingleChannel(channel);
		return fixedAmplitudeConstantTerm[channel] = getAmplitudeConstantTerm(channel);
	}

	/**
	 * Convert a raw amplitude to dB re 1 micropascal based on
	 * calibration information held in the AcquisitionController
	 * for an array of double data
	 * 
	 * @param rawAmplitude raw amplitude (should be -1 < rawAmplitude < 1)
	 * @param channel channel number (MUST be a channel 0 - 31, not a sequence number)
	 * @return amplitude in dB re 1 uPa.
	 */
	public double[] rawAmplitude2dB(double[] rawAmplitude, int channel){
		double[] ans = new double[rawAmplitude.length];
		prepareFastAmplitudeCalculation(channel);
		for (int i = 0; i < rawAmplitude.length; i++) {
			ans[i] = rawAmplitude2dB(rawAmplitude[i], channel, true);
		}
		return ans;
	}
	/**
	 * Convert the amplitude of fft data into a spectrum level measurement in
	 * dB re 1 micropacal / sqrt(Hz).
	 * @param fftAmplitude magnitude of the fft data (not the magnitude squared !)
	 * @param channel channel number (MUST be a channel, and not a sequence number)
	 * @param sampleRate sample rate - this needs to be sent, since this function is 
	 * often called from decimated data, in which case the sample rate will be different. 
	 * @param fftLength length of the FFT (needed for Parsevals correction) 
	 * @param isSquared is magnitude squared (in which case sqrt will be taken). 
	 * @param fast use fast calculation (after call to prepareFastAmplitudeCalculation(...).
	 * @return spectrum level amplitude.
	 */
	public double fftAmplitude2dB(double fftAmplitude, int channel, float sampleRate, int fftLength, boolean isSquared, boolean fast){
		if (isSquared) {
			fftAmplitude = Math.sqrt(fftAmplitude);
		}
		// correct for PArsevel (1/sqrt(fftLength and for the fact that the data were summed
		// over a fft length which requires an extra 1/sqrt(fftLength) correction.
		fftAmplitude /= fftLength;
		// allow for negative frequencies
		fftAmplitude *= sqrt2;
		// thats the energy in an nHz bandwidth. also need bandwidth correction to get
		// to spectrum level data
		double binWidth = sampleRate / fftLength;
		fftAmplitude /= Math.sqrt(binWidth);
		double dB = rawAmplitude2dB(fftAmplitude, channel, fast);
		return dB;
	}
	
	/**
	 * Convert the amplitude of fft data into a spectrum level measurement in
	 * dB re 1 micropacal / sqrt(Hz) for an array of double values.
	 * @param fftAmplitude magnitude of the fft data (not the magnitude squared !)
	 * @param channel the channel number (MUST be a channel, and not a sequence number)
	 * @param fftLength lengthof the fft (needed for Parsevals correction) 
	 * @return spectrum level amplitude.
	 */
	public double[] fftAmplitude2dB(double[] fftAmplitude, int channel, float sampleRate, int fftLength, boolean isSquared){
		double[] ans = new double[fftAmplitude.length];
		prepareFastAmplitudeCalculation(channel);
		for (int i = 0; i < fftAmplitude.length; i++) {
			ans[i] = fftAmplitude2dB(fftAmplitude[i], channel, sampleRate, fftLength, isSquared, true);
		}
		return ans;
	}

	/**
	 * Convert the amplitude of fft data into a  level measurement in
	 * dB re 1 micropacal / sqrt(Hz).
	 * <p>
	 * Note that this function differs from fftAmplitude2dB in that this one used the 
	 * FFT length to correct for Parsevals theorum and integratin over the length of the 
	 * FFT, but it does NOT convert the result to a spectrum level measurement.  
	 * @param fftAmplitude magnitude of the fft data (not the magnitude squared !)
	 * @param fftLength lengthof the fft (needed for Parsevals correction) 
	 * @return  level amplitude in dB
	 */
	public double fftBandAmplitude2dB(double fftAmplitude, int channel, int fftLength, boolean isSquared, boolean fast){
		if (isSquared) {
			fftAmplitude = Math.sqrt(fftAmplitude);
		}
		// correct for PArsevel (1/sqrt(fftLength and for the fact that the data were summed
		// over a fft length which requires an extra 1/sqrt(fftLength) correction.
		fftAmplitude /= fftLength;
		// allow for negative frequencies
		fftAmplitude *= sqrt2;
		// thats the energy in an nHz bandwidth. also need bandwidth correction to get
		// to spectrum level data
		//		double binWidth = sampleRate / fftLength;
		//		fftAmplitude /= Math.sqrt(binWidth);
		double dB = rawAmplitude2dB(fftAmplitude, channel, fast);
		return dB;
	}

	/**
	 * Converts dB in micropascal to ADC counts on a 0 - 1 scale. 
	 * @param channel channel number, i.e. channel index 0 - 31 NOT a bitmap. 
	 * @param dBMuPascal db in micropascal
	 * @return ADC counts on a 0-1 scale. 
	 */
	public double dbMicropascalToSignal(int channel, double dBMuPascal) {
		double db = dBMuPascal + getAmplitudeConstantTerm(channel);
		double volts = Math.pow(10, db/20);
		double re1 = volts / (acquisitionControl.acquisitionParameters.voltsPeak2Peak/2);
		return re1;
	}

	/**
	 * @return Returns the acquisitionControl.
	 */
	public AcquisitionControl getAcquisitionControl() {
		return acquisitionControl;
	}

	/**
	 * @return the rawDataBlock
	 */
	public PamRawDataBlock getRawDataBlock() {
		return rawDataBlock;
	}

	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {
		
//		System.out.println("AquisitionProcess: GetofflineData: " + offlineLoadDataInfo.getCurrentObserver().getObserverName())
		
		if (acquisitionControl.getOfflineFileServer() == null) {
			return PamDataBlock.REQUEST_NO_DATA;
		}
		if (acquisitionControl.getOfflineFileServer().loadData(getRawDataBlock(), offlineLoadDataInfo, null)) {
			return PamDataBlock.REQUEST_DATA_LOADED;
		}
		else {
			return PamDataBlock.REQUEST_NO_DATA;
		}
	}
	
	/**
	 * Get the total number of samples acquired by a particular channel. 
	 * @param iChannel
	 * @return number of samples. 
	 */
	public long getTotalSamples(int iChannel) {
		return newDataQueue.getSamplesIn(iChannel);
	}

	public long getStallCheckSeconds() {
		if (runningSystem == null) {
			return 60;
		}
		return runningSystem.getStallCheckSeconds();
	}

	long lastStallCheckTime = 0;
	long lastStallCheckSamples = 0;
	boolean lastStallState = false;
	/**
	 * 
	 * @return if the acquisition system seems to have stopped acquiring data. 
	 */
	public boolean isStalled() {
		long samples = getTotalSamples(0);
		long now = System.currentTimeMillis();
		long stallCheckSecs = getStallCheckSeconds();
		long runTime = now - PamCalendar.getSessionStartTime();
		if (runTime < stallCheckSecs * 2000 && samples == 0) {
			return false; // give it plenty of time to get started. 
		}
		if (now - lastStallCheckTime < (stallCheckSecs*1000) && lastStallState == false) {
			return lastStallState;
		}
		double aveSR = (double) (samples - lastStallCheckSamples) / (double) (now-lastStallCheckTime) * 1000.;
		// check on lastStallCheckTime > 0 will stop this doing anything the first time through when it will always get a stupid small value. 
		boolean stalled =  (aveSR < getSampleRate() * .7 && lastStallCheckTime > 0);
		if (stalled) {
			System.out.println(String.format("System stalled samples %d, lastCallSamples %d, mean SR = %5.1f", samples, lastStallCheckSamples, aveSR));
		}
		lastStallCheckSamples = samples;
		lastStallCheckTime = now;
		lastStallState = stalled;
		return lastStallState;
	}

	/**
	 * Get the DAQ status data block. Contains data unit with info on sample numbers etc. 
	 * @return the daqStatusDataBlock
	 */
	public PamDataBlock<DaqStatusDataUnit> getDaqStatusDataBlock() {
		return daqStatusDataBlock;
	}

	@Override
	public InputStoreInfo getStoreInfo(boolean detail) {
		if (runningSystem instanceof DataInputStore) {
			return ((DataInputStore) runningSystem).getStoreInfo(detail);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean setAnalysisStartTime(long startTime) {
		if (runningSystem instanceof DataInputStore) {
			return ((DataInputStore) runningSystem).setAnalysisStartTime(startTime);
		}
		else {
			return false;
		}
	}


	

}
