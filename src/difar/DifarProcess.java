package difar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import Filters.FIRArbitraryFilter;
import GPS.GPSControl;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamUtils.LatLong;
import PamUtils.MatrixOps;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import Spectrogram.WindowFunction;
import difar.DifarParameters.DifarOutputTypes;
import difar.DifarParameters.DifarTriggerParams;
import difar.DifarParameters.SpeciesParams;
import difar.calibration.CalibrationDataBlock;
import difar.calibration.CalibrationHistogram;
import difar.calibration.CalibrationLogging;
import difar.calibration.CalibrationProcess;
import difar.demux.AmmcDemux;
import difar.demux.DifarDemux;
import difar.demux.DifarResult;
import difar.demux.NativeDemux;
import difar.crossings.CrossingRecorder;
import difar.crossings.DifarCrossing;
import difar.crossings.DifarCrossingDataBlock;
import difar.crossings.DifarCrossingLogging;
import difar.crossings.DifarCrossingSubLogging;
import difar.display.DIFARUnitControlPanel;
import difar.display.DifarOverlayGraphics;
import generalDatabase.lookupTables.LookupItem;
import difar.targetmotion.TargetMotionResult;
import difar.targetmotion.Simplex2D;
import warnings.PamWarning;
import warnings.WarningSystem;

public class DifarProcess extends PamProcess {

	/** Candidate matches for recent detections, for the matching display. */
	private DifarMatchLog matchLog = new DifarMatchLog();

	private DifarControl difarControl;

	private PamRawDataBlock rawDataSource;
	
	private ArrayList<PamDataBlock<PamDataUnit>> triggerDataSources;

	private DifarDataBlock queuedDifarData;

	private DifarDemux nativeDemux = new NativeDemux(difarControl);
	private DifarDemux ammcDemux = new AmmcDemux();
	private DifarDemux difarDemux = ammcDemux;
	
	private DifarDataBlock processedDifarData;

	/** Crossings of saved clips, stored in the database only. */
	private DifarCrossingDataBlock crossingDataBlock;

	/** Makes crossing units as clips are saved. */
	private CrossingRecorder crossingRecorder;
	
	private CalibrationDataBlock calibrationDataBlock;

	private ArrayList<TriggerObserver> triggerObservers;

	private CalibrationHistogram[] calTrueBearingHistograms = new CalibrationHistogram[PamConstants.MAX_CHANNELS];
	private CalibrationHistogram[] calCorrectionHistograms = new CalibrationHistogram[PamConstants.MAX_CHANNELS];
	private CalibrationProcess[] calibrationProcesses = new CalibrationProcess[PamConstants.MAX_CHANNELS];

	private DifarDemuxWorker demuxWorker;
	
	private Timer autoSaveTimer;

	private Simplex2D simplex2D;
	
	private FIRArbitraryFilter difarAmplitudeFilter;
	
	public DifarProcess(DifarControl difarControl) {
		super(difarControl, null);
		
		this.difarControl = difarControl;
		queuedDifarData = new DifarDataBlock("Queued DIFAR Data", difarControl, true, this, 0);
		processedDifarData = new DifarDataBlock("Processed DIFAR Data", difarControl, false, this, 0);
		queuedDifarData.setOverlayDraw(new DifarOverlayGraphics(difarControl, queuedDifarData, true));
		StandardSymbolManager symbolManager = new StandardSymbolManager(queuedDifarData, DifarOverlayGraphics.defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		queuedDifarData.setPamSymbolManager(symbolManager);
		processedDifarData.setOverlayDraw(new DifarOverlayGraphics(difarControl, processedDifarData, false));
		processedDifarData.setPamSymbolManager(new StandardSymbolManager(processedDifarData, DifarOverlayGraphics.defaultSymbol, true));
		processedDifarData.SetLogging(new DifarSqlLogging(difarControl, processedDifarData));
		processedDifarData.setBinaryDataSource(new DifarBinaryDataSource(difarControl, processedDifarData));
		processedDifarData.setShouldLog(true);
		processedDifarData.setShouldBinary(true);
		processedDifarData.setClearAtStart(false);
		addOutputDataBlock(queuedDifarData);
		addOutputDataBlock(processedDifarData);
		crossingDataBlock = new DifarCrossingDataBlock("DIFAR Crossings", this);
		String crossingTable = difarControl.getUnitName() + " Crossings";
		DifarCrossingLogging crossingLogging = new DifarCrossingLogging(crossingTable, crossingDataBlock);
		crossingLogging.setSubLogging(new DifarCrossingSubLogging(crossingTable + " Children", difarControl, crossingDataBlock));
		crossingDataBlock.SetLogging(crossingLogging);
		crossingDataBlock.setShouldLog(true);
		crossingDataBlock.setClearAtStart(false);
		crossingDataBlock.setNaturalLifetime(24 * 3600);
		addOutputDataBlock(crossingDataBlock);
		crossingRecorder = new CrossingRecorder(this, difarControl);
		calibrationDataBlock = new CalibrationDataBlock(this);
		calibrationDataBlock.SetLogging(new CalibrationLogging(this, calibrationDataBlock));
		calibrationDataBlock.setShouldLog(true);
		/*
		 * A day, like the clips. A calibration is worth keeping for as long as
		 * the detections it applies to.
		 */
		calibrationDataBlock.setNaturalLifetime(24 * 3600);
		addOutputDataBlock(calibrationDataBlock);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == rawDataSource) {
			newRawData(o, arg);
		}
		else if (o == queuedDifarData) {
			newQueuedData(o, arg);
		}
	}

	private void newQueuedData(PamObservable o, PamDataUnit arg) {
	}

	/**
	 * Send a difar unit off for processing in a worker thread. 
	 * For now, only allow one of these, but may consider having more in 
	 * the future. 
	 * @param difarDataUnit
	 */
	public void queueDemuxProcess(DifarDataUnit difarDataUnit) {
		demuxWorker = new DifarDemuxWorker(difarDataUnit);
		demuxWorker.execute();
	}
	
	/**
	 * 
	 * @return whether the demux worker is demuxing/calculating a difargram or not;
	 */
	boolean isProcessing(){
		
		return !( demuxWorker==null || demuxWorker.isDone() );
	}

	
	class DifarDemuxWorker extends SwingWorker<Integer, DemuxWorkerMessage> implements DemuxObserver{

		private DifarDataUnit difarDataUnit;
		private long startMillis;

		public DifarDemuxWorker(DifarDataUnit difarDataUnit) {
			super();
			this.difarDataUnit = difarDataUnit;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				startMillis = System.currentTimeMillis();
				processDifarUnit(difarDataUnit, this);
			}
			catch (Exception e) {
				// print the stack trace here, otherwise doinbackground wont' show exceptions
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Called from demux worker to update on percentage progress
		 * @param progressPercent
		 */
		public void setBScanProgress(int iStatus, double percent) {
			publish(new DemuxWorkerMessage(difarDataUnit, iStatus, System.currentTimeMillis()-startMillis, percent));
		}


		public void ppublish(DemuxWorkerMessage message) {
			publish(message);
		}

		@Override
		protected void process(List<DemuxWorkerMessage> chunks) {
			for (int i = 0; i < chunks.size(); i++) {
				difarControl.getDemuxProgressDisplay().newMessage(chunks.get(i));
			}
		}

		@Override
		protected void done() {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.DemuxComplete, difarDataUnit));
			if (difarControl.getDifarParameters().autoSaveDResult &&
					difarDataUnit.canAutoSave() &&
					!difarControl.isViewer() ){
				cancelAutoSaveTimer();
				autoSaveTimer = new AutoSaveTimer(difarControl.getCurrentDemuxedUnit());
				autoSaveTimer.start();
			}
		}

		@Override
		public void setStatus(double percentComplete, boolean lock75,
				boolean lock15) {
			publish(new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_INDEMUXCALC, 
					System.currentTimeMillis()-startMillis, 
					percentComplete, lock75, lock15));
		}


	}
	
	class AutoSaveTimer extends Timer{
		/**
		 * @param delay
		 * @param listener
		 */
		public AutoSaveTimer(DifarDataUnit difarDataUnit) {
			super(100, new AutoSaveListener(difarDataUnit));
			setRepeats(true);
			start();
		}
		
		
	}
	
	class AutoSaveListener implements ActionListener{
		
		float time=0.0f;
		DifarDataUnit difarDataUnit;
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		/**
		 * @param difarDataUnit
		 */
		public AutoSaveListener(DifarDataUnit difarDataUnit) {
			this.difarDataUnit=difarDataUnit;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			time +=0.1f;

			// update progress bar to display autosave timer
			double percentComplete = (double) (time/difarControl.getDifarParameters().autoSaveTime);
			DemuxWorkerMessage msg = new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_AUTOSAVEPENDING, 
					0L, percentComplete);
			difarControl.getDemuxProgressDisplay().newMessage(msg);

			if (time>difarControl.getDifarParameters().autoSaveTime){
				if (difarControl.getCurrentDemuxedUnit()!=difarDataUnit || difarControl.getCurrentDemuxedUnit()==null){
					// This situation should not occur, but leave this here to cleanup in case I've missed something
					time = 0;
					cancelAutoSaveTimer();
					this.difarDataUnit = null;
					msg = new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_SAVED, 
							0L, 0);
					difarControl.getDemuxProgressDisplay().newMessage(msg);
					return;
				}
				if (difarControl.getDifarParameters().autoSaveDResult
						&&difarControl.getCurrentDemuxedUnit()==difarDataUnit
						&&difarControl.getCurrentDemuxedUnit().canAutoSave()){
					//Decide on saving range then Save it
					
					if (difarControl.getDifarParameters().autoSaveAngleOnly){
						difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.SaveDatagramUnitWithoutRange, difarControl.getCurrentDemuxedUnit()));
					}else{
						difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.SaveDatagramUnit, difarControl.getCurrentDemuxedUnit()));
					}
					
					DIFARUnitControlPanel ducp = difarControl.getDifarUnitControlPanel();
					if (ducp!=null){
						ducp.enableControls();
					}
				}
				
			}
		}
	}
	

	/**
	 * Process a queued data unit - in normal operation, this involves
	 * first demuxing and decimating the data in order to get the waveforms
	 * for the three sensors, then calculating the difargram. <br>
	 * In viewer mode the demux stage can be skipped since only teh decimated demuxed data
	 * were stored in the first place.
	 * <p>This is called from within a SwingWorker thread, and can call back through 
	 * that SwingWorker to update on progress.   
	 * @param difarDataUnit 
	 * @param demuxWorker 
	 */
	public void processDifarUnit(DifarDataUnit difarDataUnit, DifarDemuxWorker demuxWorker) {

		long startTime = System.currentTimeMillis();

		SpeciesParams sP;
		//TODO sort frequency ranges-this is probably where it should really be set, as we now have the species paramseters
		if (difarDataUnit.isVessel()){
			sP = difarControl.getDifarParameters().findSpeciesParams(DifarParameters.CalibrationClip);

		}else {
			sP = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit);
		}
		difarDataUnit.setDisplaySampleRate(sP.sampleRate);
		if (difarDataUnit.triggerName == null) {
			/*
			 * The trigger name is not stored in the binary file, so it is null
			 * for a unit loaded in viewer mode. Its frequency limits were set
			 * when it was first processed, so leave them alone.
			 */
		} else if (difarDataUnit.triggerName.equals(difarControl.getUnitName())) { // User detection
			if (!sP.useMarkedBandsForSpectrogramClips ){
				double[] frequency = {sP.processFreqMin, sP.processFreqMax};
				difarDataUnit.setFrequency(frequency);
			}
		} else { //Check whether to override auto detection freq limits with DIFAR limits
			if (!sP.useDetectionLimitsForTriggeredDetections ){ // Auto-detection
				double[] frequency = {sP.processFreqMin, sP.processFreqMax};
				difarDataUnit.setFrequency(frequency);
			}
		}
		
		
		demuxDataUnit(difarDataUnit, demuxWorker, startTime);

		calculateDifarGram(difarDataUnit, demuxWorker, startTime);

		estimateTrackedGroup(difarDataUnit);
		
	}

	private boolean demuxDataUnit(DifarDataUnit difarDataUnit, DifarDemuxWorker demuxWorker, long startTime) {

		double[] difarClip = difarDataUnit.getMultiplexedData();
		if (difarClip == null) {
			return false; // this will happen as normal behaviour in viewer mode. 
		}

		if (demuxWorker != null) {
			demuxWorker.ppublish(new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_START, System.currentTimeMillis()-startTime));
		}

		float sampleRate = getSampleRate();
		int decimationFactor = (int) (sampleRate / difarDataUnit.getDisplaySampleRate());

		/*
		 * Respect user's choice of demodulator
		 */
		double gainCorrection = 1;
		switch (difarControl.difarParameters.demuxType){
		case GREENERIDGE:
			difarDemux = nativeDemux;
			gainCorrection = 1;
 			break;
		case AMMC_EXPERIMENTAL:
			difarDemux = ammcDemux;
			gainCorrection = 1;
			break;
		}	
		DifarResult difarResult = difarDemux.processClip(difarClip, sampleRate, decimationFactor, demuxWorker, difarDataUnit);

		/*
		 * Clean up the demultiplexed data
		 */
		boolean[][] lockArrays = new boolean[2][];
		// everyting now already decimated, so no need to do anything at all !
		lockArrays[0] = difarResult.getLock_75();
		lockArrays[1] = difarResult.getLock_15();

		double[][] demuxedDecimatedData = difarResult.getDataArrays();
		
		/*
		 * Truncate to actual call - ignoring initial seconds. 
		 * also consider what time the lock was found on the carrier. 
		 * Call bScan with decimated data and correct frequency. 
		 */

		/* 
		 * See if the data have extra time at the start (e.g. standard 10s before each marked area)
		 * and if they have, remove it here. 
		 * Vessel clips probably won't have this, but auto detections and marked spectrogram triggers will.
		 * TODO: Use the sample rate from SpeciesParameters rather than displaySampleRate, which I believe is wrong
		 */
		double preSeconds = difarDataUnit.getPreMarkSeconds();
		int preSamples = (int) (preSeconds * sampleRate/decimationFactor);
		double[][] croppedDemuxData = demuxedDecimatedData;
		boolean[][] croppedLockData = lockArrays;
		// in viewer mode, and for ammcDemux lockData will be null, so create it !
		//TODO: Put all lock related code into the Greeneridge demux classes
		if (lockArrays == null) {
			lockArrays = createFalseLockData(2, demuxedDecimatedData[0].length);
		}
		if (preSamples > 0) {
			croppedDemuxData = new double[demuxedDecimatedData.length][];
			croppedLockData = new boolean[lockArrays.length][];
			for (int i = 0; i < 3; i++) {
				croppedDemuxData[i] = Arrays.copyOfRange(demuxedDecimatedData[i], preSamples, demuxedDecimatedData[i].length);
			}
			//TODO: Put all lock related code into Greeneridge demux
			for (int i = 0; i < 2; i++) {
				croppedLockData[i] = Arrays.copyOfRange(lockArrays[i], preSamples, lockArrays[i].length);
			}
		}
		
		/*
		 * Acoustic data from Greeneridge demodulator are only valid when the
		 * demodulator has locked onto both the 7.5 kHz and 15 kHz tone. So we
		 * need to find the longest continuous block of locked data and remove
		 * all of the unlocked data.
		 * 
		 * Find indicies of the longest contiguous block of locked data There's
		 * probably a faster way to do this than iterating over the entire
		 * array...
		 * TODO: Put all lock related code into Greeneridge demux
		 */
		int currentStart=0, currentEnd=0;
		int maxStart=0, maxEnd=0;
		int maxLockCount = 0, currentLockCount=0;
		boolean bothLocked;
		for (int i=0; i < croppedLockData[0].length; i++){
			bothLocked = croppedLockData[0][i] & croppedLockData[1][i];

			// Start or continue a continuous lock
			if (bothLocked){
				if (currentLockCount == 0) currentStart=i;
				currentLockCount++;
			}
			// End a continuous lock, or the end of the data
			if (!bothLocked || i==croppedLockData[0].length-1)
				currentEnd = i;
				if (currentLockCount > maxLockCount){
					maxLockCount = currentLockCount;
					maxStart = currentStart;
					maxEnd = currentEnd;
			}
		}
		
		/*
		 * Crop the largest continuous section of locked data Demuxed units
		 * should only have valid demodulated data, so downstream processing
		 * should not worry about "locks." However, downstream processing will
		 * have to worry about demuxed data being shorted than the bScan
		 * FFTLength or null.
		 * 
		 * As a workaround, pad the demux signals with zeros.
		 * 
		 * TODO: Create code that allows demultiplexing to fail gracefully
		 * rather than continue in this incorrect manner.
		 */
		int nSamples = (maxEnd - maxStart);
		int minSamples = getDemuxFFTLength(difarDataUnit); 
		
		if (nSamples < minSamples) {
			int nPadding = minSamples - nSamples;
			maxEnd = maxEnd + nPadding;
			nSamples = (maxEnd - maxStart);
		}
		for (int i = 0; i < 3; i++) {
			croppedDemuxData[i] = Arrays.copyOfRange(croppedDemuxData[i], maxStart, maxEnd);
		}
		for (int i = 0; i < 2; i++) {
			croppedLockData[i] = Arrays.copyOfRange(croppedLockData[i], maxStart, maxEnd);
		}

		
		/*
		 * It appears that the Greeneridge demultiplexer has a bug that
		 * manifests itself as a gain imbalance between the EW and NS channel.
		 * The NS channel appears to be ~1.1 V higher than the EW channel. The
		 * workaround is simply to multiply the EW channel by 1.1, or divide the
		 * NS channel by 1.1.
		 * TODO Consider making this a user-controlled parameter.
		 */
		 for (int i=0; i<croppedDemuxData[1].length; i++){
			 croppedDemuxData[1][i] *= gainCorrection;
		 }

		/*
		 * Commented code below uses the 4-quadrant arctangent function to
		 * estimate the magnetic bearing. When the input signal is a pure tone,
		 * this bearing can be directly compared to that generated from the
		 * DifarGram.
		 */
		// Double EwRMS = getRMS(croppedDemuxData[1]);
		// Double NsRMS = getRMS(croppedDemuxData[2]);
		// Double angle = Math.atan2(EwRMS, NsRMS) * 180 / Math.PI;
		// System.out.println("Atan2 angle: " + angle);
		long duration = (long) (nSamples/(sampleRate/decimationFactor));
		difarDataUnit.setSampleDuration(duration);
		difarDataUnit.setDemuxedDecimatedData(croppedDemuxData);
		difarDataUnit.setLockDecimatedData(croppedLockData);

		if (demuxWorker != null) {
			demuxWorker.ppublish(new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_DONEDEMUX, System.currentTimeMillis()-startTime));
		}

		if (difarResult == null) {
			System.out.println("demuxed data null!! processing failed");
			return false;
		}

		nSamples = croppedDemuxData[1].length;
		if (nSamples < getDemuxFFTLength(difarDataUnit))
			return false;
		duration = (long) (nSamples/difarDataUnit.getDisplaySampleRate()*difarDataUnit.getSourceSampleRate());
		difarDataUnit.setSampleDuration(duration);
		difarDataUnit.setDemuxedDecimatedData(croppedDemuxData);


		return true;
	}
	
	
	/**
	 * From the lock data and the three demultiplexed channels, caluclate the 
	 * DIFAR surface. 
	 * @param difarDataUnit data unit
	 * @param demuxWorker worker thread for callback messages
	 * @param startTime start time. 
	 */
	private void calculateDifarGram(DifarDataUnit difarDataUnit, DifarDemuxWorker demuxWorker, long startTime) {
		double[][] demuxData = difarDataUnit.getDemuxedDecimatedData();
		boolean[][] lockData = difarDataUnit.getLockDecimatedData();

		SpeciesParams sP = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit);
		int FFTLength = sP.FFTLength;
		int FFTHop = sP.FFTHop;
		float sampleRate = sP.sampleRate;
		double[] frequency = difarDataUnit.getFrequency();
		int nAngleSections = sP.getnAngleSections();
		DifarOutputTypes difarOutputType = sP.difarOutputType;
		BScan bScan = new BScan(difarControl,demuxData,
				FFTLength, FFTHop, sampleRate, frequency, nAngleSections, difarOutputType,
				demuxWorker);


		double[][] difarSurface = bScan.getSurfaceData();
		difarSurface = MatrixOps.getAbsMatrix(difarSurface);
		difarDataUnit.setDifarGain(bScan.getDifarGain());

		difarDataUnit.setSurfaceData(difarSurface);
		if (difarControl.getDifarParameters().useSummaryLine){
			difarDataUnit.setMaximumAngleSummary(createMaxAngleSummary(difarDataUnit, difarDataUnit.getFrequency()));
			difarDataUnit.setSurfaceSummary(createSummaryLine(difarDataUnit, difarDataUnit.getFrequency()));
		}else {
			difarDataUnit.setSurfaceSummary(createSummaryLine(difarDataUnit, difarDataUnit.getFrequency()));
			difarDataUnit.setMaximumAngleSummary(createMaxAngleSummary(difarDataUnit, difarDataUnit.getFrequency()));
		}
		
		if (demuxWorker != null) {
			demuxWorker.ppublish(new DemuxWorkerMessage(difarDataUnit, DemuxWorkerMessage.STATUS_DONEDIFARCALC, System.currentTimeMillis()-startTime));
		}

		DifarLocalisation difarLocalisation = new DifarLocalisation(difarDataUnit,
				LocContents.HAS_BEARING, difarDataUnit.getChannelBitmap());
		difarLocalisation.setBearingError(difarControl.getDifarParameters().bearingError);
		difarDataUnit.setLocalisation(difarLocalisation);

		getDifarRangeInfo(difarDataUnit);
		
		getDifarAmplitude(difarDataUnit);
		
		
		if (demuxWorker == null) {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.DemuxComplete, difarDataUnit));
		}

	}

	/**
	 * Match the selected bearing to the nearest mean-bearing to a group, and
	 * Make sure that the matching group is highlighted in the groups panel. 
	 * @param difarDataUnit
	 */
	public void estimateTrackedGroup(DifarDataUnit difarDataUnit) {
		if (difarControl.isViewer()){
			try{
			difarControl.setCurrentlySelectedGroup(difarDataUnit.getTrackedGroup());
			} finally {
				difarControl.setCurrentlySelectedGroup(DifarParameters.DefaultGroup);
			}
			return;
		}
		if (difarDataUnit.isVessel()) {
			difarControl.setCurrentlySelectedGroup(DifarParameters.DefaultGroup);
			difarDataUnit.setTempGroup(DifarParameters.DefaultGroup);
		}
		String bestGroup = difarControl.getTrackedGroupProcess().getTrackedGroups().getNearestGroup(difarDataUnit);
		difarControl.setCurrentlySelectedGroup(bestGroup);
		
		String selectedGroup = difarControl.getCurrentlySelectedGroup();
		// now check that the group exists in the datablock AND hasn't been removed from the display
		if (bestGroup==null || !difarControl.isTrackedGroupSelectable(bestGroup) || 
				!bestGroup.equals(selectedGroup)){ 
			bestGroup = DifarParameters.DefaultGroup;
		}						
		
		difarDataUnit.setTempGroup(bestGroup);
		difarControl.setCurrentlySelectedGroup(bestGroup);
	}
	
	/**
	 * creates false lock data for viewer saying loacked all the time
	 */
	boolean[][] createFalseLockData(int n, int m) {
		boolean[][] l = new boolean[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				l[i][j] = true;
			}
		}
		return l;
	}
	/**
	 * Convert a point on the difar grid to an angle in degrees. 
	 * @param difarDataUnit2 
	 * @param difarGridPos position on Grid (double since may interpolate)
	 * @return angle in degrees
	 */
	public double difarGridToDegrees(DifarDataUnit difarDataUnit, double difarGridPos) {
		SpeciesParams sP = difarControl.difarParameters.findSpeciesParams(difarDataUnit);
		int nAngleSections = sP.getnAngleSections();
		return difarGridPos*360./(double) nAngleSections;
	}
	
	/**
	 * 
	 * @param difarDataUnit difar data unit. 
	 * @param gridPos grid position
	 * @return Frequency in Hz. 
	 */
	public double difarGridToFrequency(DifarDataUnit difarDataUnit, double gridPos) {
		double[][] difarGram = difarDataUnit.getSurfaceData();
		return difarGridToFrequency(difarGram[0].length, difarDataUnit.getDisplaySampleRate()/2, gridPos);
	}

	/**
	 * convert a difar grid position to a freqeuncy. 
	 * @param nFreqPoints number of frequency points in the grid
	 * @param niquist niquist frequency for the decimated data 
	 * @param gridPos grid position
	 * @return Frequency in Hz. 
	 */
	public double difarGridToFrequency(int nFreqPoints, double niquist, double gridPos) {
		return gridPos/nFreqPoints * niquist;
	}
	/**
	 * Create a single line summary of DIFAR level against angle within a selected frequency range
	 * @param difarDataUnit
	 * @param freqRange
	 * @return
	 */
	public double[] createSummaryLine(DifarDataUnit difarDataUnit, double[] freqRange) {
		freqRange = checkFrequencyRange(difarDataUnit.getDisplaySampleRate()/2, freqRange);
		double[][] surfData = difarDataUnit.getSurfaceData();
		if (surfData == null || surfData.length < 1 || surfData[0].length < 1) {
			return null;
		}
		int nAng = surfData.length;
		int nFreq = surfData[0].length;
		int[] fBin = new int[2];
		for (int i = 0; i < 2; i++) {
			fBin[i] = (int) Math.round(freqRange[i] / (difarDataUnit.getDisplaySampleRate()/2) * nFreq);
			fBin[i] = Math.max(0, Math.min(nFreq, fBin[i]));
		}
		double[] summaryLine = new double[nAng];
		for (int i = 0; i < nAng; i++) {
			for (int j = fBin[0]; j < fBin[1]; j++) {
				summaryLine[i] += surfData[i][j];
			}
		}

		difarDataUnit.setSurfaceSummary(summaryLine);
		/**
		 * Now find the current maximum position in terms of angle and frequency within the frequency range
		 * angle will be easy since it's just the max of surfaceSummary. 
		 */
		double maxVal = Double.MIN_VALUE;
		int maxAngleInd = -1;
		for (int i = 0; i < summaryLine.length; i++) {
			if (summaryLine[i] >= maxVal) {
				maxVal = summaryLine[i];
				maxAngleInd = i;
			}
		}
		if (maxAngleInd >= 0) {
			if (maxAngleInd == 0 || maxAngleInd == summaryLine.length-1) {
				difarDataUnit.setSelectedAngle(difarGridToDegrees(difarDataUnit, maxAngleInd));
			}
			else {
				if (pickBearing(difarDataUnit)) {
					difarDataUnit.setSelectedAngle(difarGridToDegrees(difarDataUnit, maxAngleInd));
				}
				difarDataUnit.setMaximumAngle(difarGridToDegrees(difarDataUnit, maxAngleInd));
			}
			// now the max frequency at that angle. 
			maxVal = Double.MIN_VALUE;
			int maxFreqInd = -1;
			for (int j = fBin[0]; j < fBin[1]; j++) {
				if (surfData[maxAngleInd][j] >=  maxVal) {
					maxVal = surfData[maxAngleInd][j];
					maxFreqInd = j;
				}
			}
			if (maxFreqInd >= 0) {
				if (pickBearing(difarDataUnit)) {
					difarDataUnit.setSelectedFrequency(difarGridToFrequency(difarDataUnit, maxFreqInd));
				}
				difarDataUnit.setMaximumFrequency(difarGridToFrequency(difarDataUnit, maxFreqInd));
			}
		}

		return summaryLine;
	}

	/**
	 * Create a summary of angles with maximum DIFAR level for each frequency
	 * @param difarDataUnit
	 * @param freqRange
	 * @return
	 */
	public double[] createMaxAngleSummary(DifarDataUnit difarDataUnit, double[] freqRange) {
		freqRange = checkFrequencyRange(difarDataUnit.getDisplaySampleRate()/2, freqRange);
		double[][] surfData = difarDataUnit.getSurfaceData();
		if (surfData == null || surfData.length < 1 || surfData[0].length < 1) {
			return null;
		}
		int nAng = surfData.length;
		int nFreq = surfData[0].length;
		int[] fBin = new int[2];
		for (int i = 0; i < 2; i++) {
			fBin[i] = (int) Math.round(freqRange[i] / (difarDataUnit.getDisplaySampleRate()/2) * nFreq);
			fBin[i] = Math.max(0, Math.min(nFreq, fBin[i]));
		}

		/**
		 * New style surface summary is the angle of maximum amplitude at each frequency.
		 * Also, find the indicies of the overall maximum point on the difargram 
		 */
		double[] summaryLine = new double[nFreq];
		double maxAtThisFrequency;
		double angleAtThisFrequency = 0;
		double thisFrequency;
		double maxVal = Double.MIN_VALUE;
		int maxFreqInd = -1;
		int maxAngleInd = -1;

		// Loop over each frequency and find the angle with the maximum energy
		for (int j = fBin[0]; j < fBin[1]; j++) { 
			maxAtThisFrequency = Double.MIN_VALUE;
			
			// Loop over all angles and see if this angle is the maximum
			for (int i = 0; i < nAng; i++) { 

				if (surfData[i][j] >= maxAtThisFrequency) {
					maxAtThisFrequency = surfData[i][j];
					angleAtThisFrequency = difarGridToDegrees(difarDataUnit, i); 
					summaryLine[j] = angleAtThisFrequency;
					
					//Find the overall maximum value and associated frequency indicies
					if (maxAtThisFrequency >= maxVal){
						maxVal = maxAtThisFrequency;
						maxAngleInd = i;
						maxFreqInd = j;
					}

					
				}
			}  // End loop over angles
			
		}// End loop over frequencies
		
		difarDataUnit.setMaximumAngleSummary(summaryLine);
		
		if (maxAngleInd >= 0) {
			if (pickBearing(difarDataUnit)) {
				difarDataUnit.setSelectedAngle(difarGridToDegrees(difarDataUnit, maxAngleInd));
			}
			difarDataUnit.setMaximumAngle(difarGridToDegrees(difarDataUnit, maxAngleInd));
		}
		if (maxFreqInd >= 0) {
			if (pickBearing(difarDataUnit)) {
				difarDataUnit.setSelectedFrequency(difarGridToFrequency(difarDataUnit, maxFreqInd));
			}
			difarDataUnit.setMaximumFrequency(difarGridToFrequency(difarDataUnit, maxFreqInd));
		}

		return summaryLine;
	}

	
	/**
	 * Check a frequency range pair against the niqust frequency
	 * @param niquist
	 * @param freqRange
	 * @return
	 */
	private double[] checkFrequencyRange(double niquist, double[] freqRange) {
		double[] newRange;
		if (freqRange == null || freqRange.length != 2) {
			newRange = new double[2];
			newRange[1] = niquist;
		}
		else {
			newRange = freqRange.clone();
			for (int i = 0; i < 2; i++) {
				newRange[i] = Math.max(0, Math.min(niquist, newRange[i]));
			}
		}
		return newRange;
	}
	
	/**
	 * Return the appropriate FFT length for the type of data unit to 
	 * be used in difargram creation 
	 * @param difarDataUnit
	 * @return FFT length
	 */
	private int getDemuxFFTLength(DifarDataUnit difarDataUnit) {
		if (difarDataUnit.isVessel()){
			return difarControl.difarParameters.findSpeciesParams(DifarParameters.CalibrationClip).FFTLength;
		}else{
			return difarControl.difarParameters.findSpeciesParams(difarDataUnit).FFTLength;
		}
		
	}
	
	/**
	 * Return the appropriate FFT length for the type of data unit to 
	 * be used in difargram creation 
	 * @param difarDataUnit
	 * @return FFT length
	 */
	private int getDemuxFFTHop(DifarDataUnit difarDataUnit) {
		if (difarDataUnit.isVessel()){
			return difarControl.difarParameters.findSpeciesParams(DifarParameters.CalibrationClip).FFTHop;
		}else{
			return difarControl.difarParameters.findSpeciesParams(difarDataUnit).FFTHop;
		}
		
	}
	
	

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		if (o == rawDataSource) {
			return difarControl.getDifarParameters().keepRawDataTime*1000L;
		}
		return 0;
	}

	/**
	 * This sends all raw data for each channel to the calibration process for that channel.
	 * It is up to the calibration process to decide whether or not to use this data to 
	 * create a new audio clip for "calibration".
	 * @param o 
	 * @param arg - Raw DIFAR audio (ie. not downsampled) 
	 */
	private void newRawData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawDataUnit = (RawDataUnit)arg;	
		
		for (int i = 0; i < calibrationProcesses.length; i++) {
			if (calibrationProcesses[i] != null) {
				if ((rawDataUnit.getChannelBitmap() & 1<<calibrationProcesses[i].getChannel()) != 0) {
				calibrationProcesses[i].newRawData(rawDataUnit);
				}
			}
		}
	}

	/**
	 * Start a buoy calibration on a specified channel
	 * @param channel channel number
	 */
	public void startBuoyCalibration(int channel) {
		getCalibrationProcess(channel).startBuoyCalibration();
	}
	/**
	 * Grab a clip for the latest buoy calibration data.
	 * @param buoyCalibrationChannel2
	 */
	public Long doBuoyCalibration(int buoyCalibrationChannel, long endSample) {
		int duration = (int) (difarControl.difarParameters.vesselClipLength * getSampleRate());
		long startSample = endSample - duration;
		double[][] rawData = null;
		try {
			rawData = rawDataSource.getSamples(startSample, duration, 1<<buoyCalibrationChannel);
		} catch (RawDataUnavailableException e) {
			System.out.println("Buoy calibration: " + e.getMessage());
			return null;
		}
		long timeMilliseconds = this.absSamplesToMilliseconds(startSample);
		double[] frequencyRange = new double[2];
		
		SpeciesParams vesParams = difarControl.difarParameters.findSpeciesParams(DifarParameters.CalibrationClip);
		
		frequencyRange[0]=vesParams.processFreqMin;
		frequencyRange[1]=vesParams.processFreqMax;
		
		
		double[] freqs = difarControl.getDifarParameters().getDifarFreqResponseFilterParams().getArbFreqs();
		double[] gains = difarControl.getDifarParameters().getDifarFreqResponseFilterParams().getArbGainsdB();
		
		DifarDataUnit difarDataUnit = new DifarDataUnit(timeMilliseconds, timeMilliseconds, startSample, duration,
				1<<buoyCalibrationChannel, null, null, rawData, timeMilliseconds, 
				null, frequencyRange, getSampleRate(), vesParams.sampleRate, freqs, gains);
		difarDataUnit.setVessel(true);
		queuedDifarData.addPamData(difarDataUnit);

		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.NewDifarUnit,difarDataUnit));
		
		return difarDataUnit.getTimeMilliseconds();
	}

	/**
	 * Called when another detector has triggered a DIFAR data unit
	 * @param o
	 * @param arg
	 */
	private void newTrigger(PamObservable o, PamDataUnit arg) {
		PamDataUnit pd = (PamDataUnit) arg;
		PamDataBlock db = (PamDataBlock) o;
		long duration = (long) (pd.getSampleDuration() * 1000 / db.getSampleRate()); //TODO is this meant to be in seconds/milliseconds - milliseconds now!
		String triggerSpeciesName = DifarParameters.Default;
	
		//primarily use detections limits
		double[] f = pd.getFrequency();
		
		//catch all
		if (f == null || f[1] == f[0]) {
			f = new double[2];
			f[0] = 0;
			f[1] = db.getSampleRate()/2;
		}

		// Lookup the trigger source and assign to a species, or use the defaults;
		// This is getting to be quite a convoluted mess, and should probably be refactored.
		// Maybe need a new class to consolidate triggerParams, triggerSpeciesName, speciesParams,
		// triggerObservers -- basically all paramters for species, triggers, and lookupTables...
		int nBlocks = difarControl.difarParameters.getNumTriggersEnabled();
		for (int i = 0; i < nBlocks; i++) {
			DifarTriggerParams triggerParams = difarControl.difarParameters.getTriggerParams(i);
			
			if (db.getDataName().equals(triggerParams.dataName)){
				triggerSpeciesName = triggerParams.speciesName;
				//System.out.println("The datablock was from " + dataName + " and will be treated as " + triggerSpeciesName);
			}

		}
		
		SpeciesParams sP = difarControl.difarParameters.findSpeciesParams(DifarParameters.Default);

		difarTrigger(pd.getChannelBitmap(), pd.getTimeMilliseconds(), duration, f, pd, sP.sampleRate, triggerSpeciesName, db.getDataName());
	}

	@Override
	public void setupProcess() {
		super.setupProcess();
		// find the data source and set up a data unit trigger.
		PamController pamController = PamController.getInstance();
		PamDataBlock newDataSource = pamController.getDataBlock(RawDataUnit.class, difarControl.difarParameters.rawDataName);
		//////////////////////////////////
		if (newDataSource==null&&rawDataSource==null){//TODO FIXME-REMOVE THIS
			ArrayList<PamDataBlock> dss = pamController.getDataBlocks(RawDataUnit.class,false);
			ArrayList<PamDataBlock> poss = new ArrayList<PamDataBlock>();
			for (PamDataBlock pdb:dss){
				if (pdb.getDataName().indexOf("Sound")!=-1){
					poss.add(pdb);
				}
			}
			if (poss.size()==1){
				newDataSource=poss.get(0);
			}
			setDataKeepTimes();
		}
		//////////////////////////////////
		if (newDataSource!=null&&newDataSource != rawDataSource) {
			rawDataSource = (PamRawDataBlock) newDataSource;
			setParentDataBlock(newDataSource);
		}
		subscribeDataBlocks();
	}

	/**
	 * Look through the list of data blocks that could contain DIFAR 
	 * detections, and subscribe to them according to the difarParameters
	 */
	public synchronized void subscribeDataBlocks() {
		unSubscribeDataBlocks();

		triggerDataSources = new ArrayList<PamDataBlock<PamDataUnit>>();
		triggerObservers = new ArrayList<TriggerObserver>();
		int numTriggers = getNumTriggers();

		PamDataBlock aDataBlock;
		DifarTriggerParams triggerParams;
		int numTriggersEnabled = 0;
		for (int i = 0; i < numTriggers; i++) {
			triggerParams = difarControl.difarParameters.getTriggerParams(i);

			if (triggerParams == null){
				continue;
			}
			aDataBlock = PamController.getInstance().getDetectorDataBlock(triggerParams.dataName);
			if (aDataBlock == null) {
				continue;
			}

			if (triggerParams.enable){
				triggerDataSources.add(numTriggersEnabled, aDataBlock);
				triggerObservers.add(numTriggersEnabled, new TriggerObserver());
				triggerDataSources.get(numTriggersEnabled).addObserver(triggerObservers.get(numTriggersEnabled));
				numTriggersEnabled++;
			}
		}
		
		
	}
	
	private void unSubscribeDataBlocks() {
		if (triggerObservers == null) {
			return;
		}

		for (int i=0; i<triggerObservers.size(); i++) {
			triggerDataSources.get(i).deleteObserver(triggerObservers.get(i));
		}
		triggerObservers.clear();
		triggerDataSources.clear();
		
	}
	
	/**
	 * 
	 * @return the total number of Detectors usable by the DIFAR module. 
	 */
	public int getNumTriggers() {
		ArrayList<PamDataBlock> acousticDataBlocks = PamController.getInstance().getDataBlocks(PamDetection.class, true);
		int numTriggers = 0;
		for (int i = 0; i < acousticDataBlocks.size(); i++) {
			PamDataBlock aDataBlock = acousticDataBlocks.get(i);
			if (aDataBlock.isCanClipGenerate()) numTriggers++;
		}
		return numTriggers;
	}
	
	@Override
	public void pamStart() {
		setDataKeepTimes();
	}

	@Override
	public void pamStop() {
		

	}
	
	/**
	 * @return the autoSaveTimer
	 */
	public Timer getAutoSaveTimer() {
		return autoSaveTimer;
	}

	public void setDataKeepTimes() {
		queuedDifarData.setNaturalLifetime(difarControl.difarParameters.queuedDataKeepTime * 60);
		processedDifarData.setNaturalLifetime(difarControl.difarParameters.processedDataKeepTime * 60);
	}

	@Override
	public void clearOldData() {
		/*
		 * Optionally clear old data from the queues. 
		 */
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL){
			if (difarControl.difarParameters.clearQueueAtStart) {
				queuedDifarData.clearAll();
				difarControl.getDifarQueue().clearQueuePanel();
			}
			if (difarControl.difarParameters.clearProcessedDataAtStart) {
				processedDifarData.clearAll();
			}
		}
		else {
			super.clearOldData();
		}
	}

	/**
	 * Called when there is a trigger caused whether by a detection or by a mark being made on the spectrogram. 
	 * @param channel  (for detections)
	 * @param signalStartMillis start time in milliseconds
	 * @param durationMillis duration in milliseconds
	 * @param f1 min frequency
	 * @param f2 max frequency
	 * @param PamDataUnit associated detection (null for spectrogram marks). 
	 * @param triggerSpeciesName 
	 * @param triggerDataBlockName 
	 * @param upperFreq 
	 */
	public void difarTrigger(int channelMap, long signalStartMillis, long durationMillis, double[] f,
			PamDataUnit pamDetection, double displaySampleRate, String triggerSpeciesName, String triggerDataBlockName) {
		int millisToPreceed=(int) (difarControl.getDifarParameters().secondsToPreceed*1000);
		long clipStartTime = signalStartMillis - millisToPreceed;
		long startSample;
		int nSamples = (int) relMillisecondsToSamples(durationMillis+millisToPreceed);
		int clipSamples = (int) relMillisecondsToSamples(durationMillis);
		double[][] rawData = new double[1][];
		try {
			double[][] rawDataAll;
			if (difarControl.isViewer()) {
				/*
				 * In the viewer, samples are counted from the start of the loaded
				 * data, not from the start of a session, so the clip is found by
				 * time, as the WAV and SPL annotations do.
				 */
				startSample = viewerSampleNumber(clipStartTime);
				rawDataAll = rawDataSource.getSamplesForMillis(clipStartTime,
						durationMillis + millisToPreceed, channelMap);
			}
			else {
				startSample = Math.max(absMillisecondsToSamples(clipStartTime), 0);
				rawDataAll = rawDataSource.getSamples(startSample, nSamples, channelMap);
			}
			if (rawDataAll != null) {
				rawData[0] = rawDataAll[0]; // is fine since getSamples was fed a channel map. 
			}
		} catch (RawDataUnavailableException e) {
			System.out.println("Error in DifarProcess.difarTrigger" + e.getMessage());
			return;
		}
		if (rawData[0] == null) {
			return;
		}

//		System.out.println(String.format("Grabbed %d samples starting at %s", rawData.length, PamCalendar.formatDateTime(signalStartMillis)));
		long triggerMilliseconds = signalStartMillis;
		if (pamDetection != null) {
			triggerMilliseconds = pamDetection.getTimeMilliseconds();
		}
		
		double[] freqs = difarControl.getDifarParameters().getDifarFreqResponseFilterParams().getArbFreqs();
		double[] gains = difarControl.getDifarParameters().getDifarFreqResponseFilterParams().getArbGainsdB();
		
		DifarDataUnit du = new DifarDataUnit(clipStartTime, triggerMilliseconds, startSample, clipSamples, 
				channelMap, null, triggerDataBlockName, rawData, signalStartMillis, pamDetection, f, 
				getSourceDataBlock().getSampleRate(), (float)displaySampleRate, freqs, gains);
	
		LookupItem speciesLookupItem = difarControl.getCurrentlySelectedSpecies();
		int numTriggers = difarControl.getDifarParameters().getNumTriggersEnabled();
		for (int i = 0; i < numTriggers; i++){
			DifarTriggerParams triggerParams = difarControl.getDifarParameters().getTriggerParams(i);
			if (triggerParams.speciesName == triggerSpeciesName)
				speciesLookupItem = difarControl.getDifarParameters().getTriggerParams(i).speciesLookupItem;
		}
		
		du.setLutSpeciesItem(speciesLookupItem);
//		System.out.println("The species is " + speciesLookupItem + " and autoProcess is " + du.canAutoProcess());
		
		queuedDifarData.addPamData(du);

		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.NewDifarUnit, du));
	}

	/**
	 * Whether demuxing should pick the strongest bearing, and its frequency,
	 * for a clip. It does for new clips in any mode; a saved clip looked at
	 * again in the viewer keeps the bearing and frequency it was saved with.
	 */
	private boolean pickBearing(DifarDataUnit difarDataUnit) {
		return !difarControl.isViewer() || difarControl.isQueued(difarDataUnit);
	}

	/**
	 * The sample number of a time in the raw data loaded in the viewer, counted
	 * the same way as the loaded data, from its first unit.
	 * @param timeMillis a time within the loaded data.
	 * @return the sample number, or 0 if no raw data is loaded.
	 */
	private long viewerSampleNumber(long timeMillis) {
		RawDataUnit firstUnit = rawDataSource.getFirstUnit();
		if (firstUnit == null) {
			return 0;
		}
		return firstUnit.getStartSample()
				+ (long) ((timeMillis - firstUnit.getTimeMilliseconds()) * rawDataSource.getSampleRate() / 1000.);
	}

	public DifarDataBlock getQueuedDifarData() {
		return queuedDifarData;
	}

	public DifarDataBlock getProcessedDifarData() {
		return processedDifarData;
	}

	/**
	 * @return the crossings of saved clips.
	 */
	public DifarCrossingDataBlock getCrossingDataBlock() {
		return crossingDataBlock;
	}

	/**
	 * @return what makes crossing units as clips are saved.
	 */
	public CrossingRecorder getCrossingRecorder() {
		return crossingRecorder;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		//		if (changeType == PamController.INITIALIZATION_COMPLETE) {
		//			difarDemux.testCalls();
		//		}
	}

	/**
	 * Do any final processing of the DIFAR data unit and add it 
	 * to the datablock. <br>
	 * Save the data unit - but depending on the type of unit (whale or vessel)
	 * if it's a vessel data unit, will work out where the boats prop was when that unit
	 * was created and work out how much angle correction to apply to the array manager
	 * for the corresponding offset. 
	 * @param difarDataUnit DIFAR data unit 
	 */
	public void finalProcessing(DifarDataUnit difarDataUnit) {
		queuedDifarData.remove(difarDataUnit);
		/*
		 * The clip moves from the queue to the saved clips, so the saved clips
		 * become its parent. Adding a unit only sets its parent when it has none,
		 * so without this a saved clip would name the queue as its block forever:
		 * crossings would record the wrong block for their clips, and could not
		 * be linked back to them in the viewer.
		 */
		difarDataUnit.setParentDataBlock(processedDifarData);
		/*
		 *  the unit may already have been plotted in which case we need to clear it's origin so that
		 *  it gets a new one based on the new angle settings. This is done after it's removed from 
		 *  the queues data, but prior to the new vessel angle being updated !  
		 */
		if (difarDataUnit.isVessel()) {
			int chan = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());

				getCalTrueBearingHistogram(chan).addData(difarDataUnit.getSelectedAngle());
				Double bc = getVesselCorrection(difarDataUnit);
				if (bc != null) {
					getCalCorrectionHistogram(chan).addData(bc);
				}
		}
		
//		getDifarAmplitude(difarDataUnit);
		difarDataUnit.saveGroup();
		difarDataUnit.clearOandAngles();

		difarDataUnit.cleanUpData();
		processedDifarData.addPamData(difarDataUnit);
		processedDifarData.sortData();

	}
	
	/**
	 * Work out the mean broad band amplitude of the signal in dB re1uPa
	 * referenced right back through all calibrations, etc. 
	 * Hopefully the difar data unit contains a power spectrum alread !
	 * @param difarDataUnit
	 */
	protected Double getDifarAmplitude(DifarDataUnit difarDataUnit) {
		SpeciesParams speciesParams = difarControl.difarParameters.findSpeciesParams(difarDataUnit);
		
		int fftLength = getDemuxFFTLength(difarDataUnit);
		int fftHop = getDemuxFFTHop(difarDataUnit);
		int channel = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		if (speciesParams != null) {
			fftLength = speciesParams.FFTLength;
			fftHop = speciesParams.FFTHop;
		}
		// Need to correct for the window processing gain (Harris 1978 Proc. IEEE. 66 pp51-83). 
		double[] windowFunction = WindowFunction.getWindowFunc(difarDataUnit.getWindowType(), fftLength);
		double windowGain = 20*Math.log10(WindowFunction.getWindowGain(windowFunction)); // in dB

		double[][] specData = difarDataUnit.getSpectrogramData(0, fftLength, fftHop);
		if (specData == null || specData.length < 1) {
			return null;
		}
		double signalStart = difarDataUnit.getPreMarkSeconds();
		int t1 = (int) (signalStart * difarDataUnit.getDisplaySampleRate() / fftHop);
		int t2 = specData.length;
		t1 = Math.max(0, Math.min(specData.length, t1));
		t2 = Math.max(0, Math.min(specData.length, t2));
		if (t1>=t2) {
			return null;
		}
		
		double[] fr = difarDataUnit.getFrequency();
		int fBin[] = new int[2];
		if (fr == null) {
			return null;
		}
		for (int i = 0; i < 2; i++) {
			fBin[i] = (int) Math.round(fr[i] * fftLength / difarDataUnit.getDisplaySampleRate());
			fBin[i] = Math.max(0, Math.min(specData[0].length-1, fBin[i]));
		}
		double totPower = 0;
		for (int t = t1; t < t2; t++) {
			for (int f = fBin[0]; f <= fBin[1]; f++) {
				totPower += specData[t][f];
			}
		}
		double meanPower = totPower/(t2-t1);
		// That's the mean power along the length of the call. 
		/*
		 * Now have to work back up to the acquisition system at the start of all this and
		 * use it's functions to turn this into a dB value. 
		 */
		AcquisitionProcess daqProcess;
		try {
			daqProcess = (AcquisitionProcess) this.getSourceProcess();
		}
		catch (ClassCastException e) {
			System.err.println("DIFAR ammplitude can't calculate since no acquisition process can be found");
			return null;
		}

		double amplitude = daqProcess.fftBandAmplitude2dB(meanPower, channel, fftLength, true, false) - windowGain;
//		System.out.println(speciesParams.lookupItemName + "( " + fr[0] + "," + fr[1] + ") RMS: " + amplitude);
		difarDataUnit.setCalculatedAmlitudeDB(amplitude);
		return amplitude;
	}


	
	/**
	 * Called just before a difar unit is stored. Looks at other channels that have recently 
	 * had a data unit stored and decides whether or not they are likely to be the same 
	 * vocalisation. If they are, calculate a crossed bearing to them both. If > 2 channels
	 * do some kind of optimised fit. 
	 * @param difarDataUnit 
	 * @return information about the range (will already have been put into affected units)
	 */
	public DIFARCrossingInfo getDifarRangeInfo(DifarDataUnit difarDataUnit) {
		/**
		 * First find a list of other channels that may match by iterating backwards through
		 * the datablocks. 
		 */
		if (difarDataUnit.isVessel()) return null;

		if (PamUtils.getNumChannels(rawDataSource.getChannelMap()) < 2) {
			return null;
		}
		
		if (difarDataUnit.getLocalisation() == null) {
			return null;
		}
		int nChan = PamUtils.getNumChannels(rawDataSource.getChannelMap());
		int thisChan = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		DifarParameters params = difarControl.getDifarParameters();

		/*
		 * Collect every detection on every other buoy that could be this call,
		 * rather than the single best overlapping one. The best overlap is not
		 * always the same call, and picking it early leaves no way back.
		 */
		ArrayList<ArrayList<PamDataUnit>> candidatesByBuoy = new ArrayList<>();
		for (int i = 0; i < nChan; i++) {
			int aChan = PamUtils.getNthChannel(i, rawDataSource.getChannelMap());
			if (aChan == thisChan) {
				continue;
			}
			candidatesByBuoy.add(getMatchingUnits(difarDataUnit, aChan, params.maxCandidatesPerBuoy));
		}

		DifarMatchSelector selector = new DifarMatchSelector(this,
				params.detectionTimingError, params.maxBearingResidual, params.maxTimeDelayResidual);
		List<DifarMatchSelector.Match> candidates = selector.selectAll(difarDataUnit,
				new ArrayList<List<PamDataUnit>>(candidatesByBuoy));
		matchLog.put(difarDataUnit, candidates);
		DifarMatchSelector.Match match = DifarMatchSelector.chooseMatch(candidates);

		return applyMatch(difarDataUnit, match, DifarCrossing.MatchChoice.AUTO);
	}

	/**
	 * Make a match the crossing for a detection, ready to be saved.
	 * <p>
	 * Used for the match chosen automatically, and again when the user picks a
	 * different one in the match selector, so both go the same way. A null
	 * match leaves the detection with no crossing.
	 * @param difarDataUnit the detection being matched.
	 * @param match the match to use, or null for none.
	 * @param choice how the match was chosen: automatically or by the operator.
	 * @return the crossing, or null if there is none.
	 */
	public DIFARCrossingInfo applyMatch(DifarDataUnit difarDataUnit, DifarMatchSelector.Match match,
			DifarCrossing.MatchChoice choice) {
		DIFARCrossingInfo crossInfo = null;
		if (match != null) {
			LatLong ll = match.getResult().getLatLong();
			DIFARTargetMotionInformation tmi =
					new DIFARTargetMotionInformation(this, new ArrayList<>(match.getUnits()));
			// check the result is vaguely sensible.
			if (tmi.getGPSReference().distanceToMiles(ll) < 1000) {
				DifarDataUnit[] matchedUnits = new DifarDataUnit[match.getUnits().size()];
				for (int i = 0; i < matchedUnits.length; i++) {
					matchedUnits[i] = (DifarDataUnit) match.getUnits().get(i);
				}
				crossInfo = new DIFARCrossingInfo(matchedUnits, match.getResult());
			}
		}
		difarDataUnit.setTempCrossing(crossInfo);
		crossingRecorder.noteChoice(difarDataUnit, crossInfo == null ? null : choice);
		if (match != null) {
			for (PamDataUnit unit : match.getUnits()) {
				((DifarDataUnit) unit).setTempCrossing(crossInfo);
			}
		}
		return crossInfo;
	}

	
	
	/**
	 * Check that the localisation result is reasonable.
	 * <p>
	 * The test is how far the fitted position sits from the measurements, in
	 * degrees and in seconds. Both are quantities an operator can picture, and
	 * both work the same way for two buoys or three. A fit that fails usually
	 * means the detections were not the same call.
	 * <p>
	 * The previous test compared chi2 with zero for two buoys. Two bearings
	 * always cross exactly, so that test could never fail.
	 * @param tmi the detections and buoy positions used for the fit.
	 * @param locResult result.
	 * @return true if the fit is close enough to its measurements.
	 */
	private boolean localisationOK(DIFARTargetMotionInformation tmi, TargetMotionResult locResult) {
		if (locResult == null || locResult.getLatLong() == null) {
			return false;
		}
		DifarParameters params = difarControl.getDifarParameters();
		DifarLocalisationResiduals residuals =
				DifarLocalisationResiduals.calculate(tmi, locResult.getLatLong());
		return residuals.isWithin(params.maxBearingResidual, params.maxTimeDelayResidual);
	}
	

	/**
	 * @return the candidate matches worked out for recent detections.
	 */
	public DifarMatchLog getMatchLog() {
		return matchLog;
	}

	/**
	 * Find the detections on another channel that could be the same call.
	 * <p>
	 * A detection qualifies if it is the same species, overlaps in frequency,
	 * and overlaps in time once the maximum delay between the two buoys is
	 * allowed for. Which of them is the call is decided later, by localising
	 * each and comparing the results.
	 * @param difarDataUnit main unit to match to
	 * @param aChan other channel number we're looking for.
	 * @param maxUnits most units to return, best overlap first. A cap is only
	 * likely to matter for an automatic detector on a noisy chorus.
	 * @return qualifying units, best overlap first. Never null.
	 */
	private ArrayList<PamDataUnit> getMatchingUnits(DifarDataUnit difarDataUnit, int aChan, int maxUnits) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		double speedOfSound = array.getSpeedOfSound();
		/*
		 * Allow for clips being marked late or early, as well as for the travel
		 * time between buoys. Otherwise a call whose clip was marked a little
		 * late on the far buoy would never be considered.
		 */
		long markingSlackMillis = (long) (difarControl.getDifarParameters().maxTimeDelayResidual * 1000.);
		GpsData thisOrigin = difarDataUnit.getOriginLatLong(false);
		DifarDataUnit otherUnit;
		ArrayList<DifarDataUnit> found = new ArrayList<>();
		ArrayList<Double> scores = new ArrayList<>();
		double[] thisFreq = difarDataUnit.getFrequency();
		long thisStart = difarDataUnit.getTimeMilliseconds();
		long thisEnd = thisStart + (long) (difarDataUnit.getDurationInSeconds() * 1000.);
		long thatStart, thatEnd;
		synchronized (processedDifarData.getSynchLock()) {
			ListIterator<DifarDataUnit> it = processedDifarData.getListIterator(PamDataBlock.ITERATOR_END);
			while (it.hasPrevious()) {
				otherUnit = it.previous();
				if (otherUnit.getChannelBitmap() != 1<<aChan) {
					continue;
				}
				if (!isSameSpecies(difarDataUnit, otherUnit)) {
					continue;
				}
				thatStart = otherUnit.getTimeMilliseconds();
				thatEnd = thatStart + (long) (otherUnit.getDurationInSeconds() * 1000.);
				long travelMillis = getTravelTimeMillis(thisOrigin, otherUnit, speedOfSound);
				if (!DifarMatchSelector.couldBeSameCall(thisStart, thatStart, travelMillis, markingSlackMillis)) {
					continue;
				}
				double fOverlap = getFreqOverlap(thisFreq, otherUnit.getFrequency());
				if (fOverlap <= 0) {
					continue;
				}
				// Overlap of the clips is used only to rank candidates within the cap.
				long tOverlap = Math.max(0, getTimeOverlap(travelMillis + markingSlackMillis,
						thisStart, thisEnd, thatStart, thatEnd));
				if (otherUnit.getLocalisation() == null) {
					continue;
				}
				double olapScore = (double) tOverlap / (double) (thisEnd-thisStart) + fOverlap / (thisFreq[1]-thisFreq[0]);
				found.add(otherUnit);
				scores.add(olapScore);
			}
		}
		/*
		 * Sort by overlap, best first, so that the cap keeps the most likely
		 * candidates. Lists are short, so a simple sort is plenty.
		 */
		ArrayList<PamDataUnit> sorted = new ArrayList<>();
		while (!found.isEmpty() && sorted.size() < maxUnits) {
			int best = 0;
			for (int i = 1; i < scores.size(); i++) {
				if (scores.get(i) > scores.get(best)) {
					best = i;
				}
			}
			sorted.add(found.remove(best));
			scores.remove(best);
		}
		return sorted;
	}
	
	/**
	 * Return true if the two units have the same species
	 * @param unit1 first 
	 * @param unit2 second difar data unit
	 * @return true if it's the same species. 
	 */
	private boolean isSameSpecies(DifarDataUnit unit1, DifarDataUnit unit2) {
		if (unit1.isVessel() && unit2.isVessel()) return true;
		if (unit1.getSpeciesCode() == unit2.getSpeciesCode()) return true;
		if (unit1.getSpeciesCode() == null) return false;
		return (unit1.getSpeciesCode().equals(unit2.getSpeciesCode()));
	}

	/**
	 * Get the overlap in frequency of two calls. 
	 * @param f1
	 * @param f2
	 * @return overlap in Hz
	 */
	private double getFreqOverlap(double[] f1, double[] f2) {
		if (f1 == null || f2 == null) {
			return -1;
		}
		return Math.min(f1[1], f2[1]) - Math.max(f1[0], f2[0]);
	}

	/**
	 * Get the maximum possible time overlap between two calls on different 
	 * hydrophones separated by some distance. For two calls that fully 
	 * overlap, the maximum overlap will simply be the duration of the 
	 * shorter of the two calls. But some calls may only partially overlap,
	 * so subtract these non-overlapping parts from the duration of those calls.  
	 * 
	 * @param sepMillis separation in millis
	 * @param start1 start time of first call in millis
	 * @param end1 end time of first call in millis
	 * @param start2 start time of second call in millis
	 * @param end2 end time of second call in millis
	 * @return overall in milliseconds or -1 if no overlap. 
	 */
	/**
	 * Longest a sound can take to travel between two buoys, in milliseconds.
	 * <p>
	 * The distance comes from the buoy positions of the two detections, which
	 * are also what the localisation uses. The array geometry cannot be used
	 * here: PamArray.getSeparation() adds hydrophone offsets to each streamer's
	 * local coordinates, and a DIFAR buoy is placed by its latitude and
	 * longitude instead, so its local coordinates are zero and the separation
	 * between any two buoys came out as zero. That limited matching to clips
	 * that overlapped in time.
	 * @param thisOrigin position of the first buoy, or null if not known.
	 * @param otherUnit a detection on the second buoy.
	 * @param speedOfSound speed of sound in metres per second.
	 * @return travel time in milliseconds, or zero if either position is unknown.
	 */
	private long getTravelTimeMillis(GpsData thisOrigin, DifarDataUnit otherUnit, double speedOfSound) {
		if (thisOrigin == null || speedOfSound <= 0) {
			return 0;
		}
		GpsData otherOrigin = otherUnit.getOriginLatLong(false);
		if (otherOrigin == null) {
			return 0;
		}
		return (long) (thisOrigin.distanceToMetres(otherOrigin) / speedOfSound * 1000.);
	}

	private long getTimeOverlap(long sepMillis, long start1, long end1, long start2, long end2) {
		if (start1 > end2 + sepMillis || start2 > end1 + sepMillis) return -1; // no overlap
		start2 = Math.min(start2, start1-sepMillis);// In case latter portion of detection2 overlaps
		end2 = Math.min(end2, end1+sepMillis); 		// In case starting portion of detection2 overlaps
		return Math.min(end2-start2, end1-start1);
	}

	/**
	 * Get a display range for a particular data unit, as will be used by the map. 
	 * Currently there is only one value which is fixed in the parameters. 
	 * @param difarDataUnit
	 * @return a range in metres. 
	 */
	public double rangeForDataType(DifarDataUnit difarDataUnit) {
		if (difarDataUnit == null) {
			return 0;
		}
		if (difarDataUnit.isVessel()) {
			return getVesselRange(difarDataUnit);
		}
		else {
			return getWhaleRange(difarDataUnit);
		}
	}

	private double getWhaleRange(DifarDataUnit difarDataUnit) {
		double rl = difarDataUnit.getAmplitudeDB();
		if (rl <= 0) {
			return difarControl.getDifarParameters().defaultLength;
		}
		switch (difarControl.getDifarParameters().propLossModel){
		case DifarParameters.PROPLOSS_GEOMETRIC:
			double f = (difarControl.getDifarParameters().nominalSourceLevel - rl) / difarControl.getDifarParameters().nominalSpreading;
			return Math.pow(10., f);
		case DifarParameters.PROPLOSS_CYLINDRICAL:
			double sphericalLoss = 10*Math.log10(difarControl.getDifarParameters().cylindricalStartDistance);
			f = (difarControl.getDifarParameters().nominalSourceLevel - rl - sphericalLoss) / 10;
			return Math.pow(10., f);
		case DifarParameters.PROPLOSS_NONE:
		default:
			return difarControl.getDifarParameters().defaultLength;
		}
	}

	public double getWhaleRange(DifarDataUnit difarDataUnit, double nominalSourceLevel) {
		double rl = difarDataUnit.getAmplitudeDB();
		if (rl <= 0) {
			return difarControl.getDifarParameters().defaultLength;
		}
		switch (difarControl.getDifarParameters().propLossModel){
		case DifarParameters.PROPLOSS_GEOMETRIC:
			double f = (nominalSourceLevel - rl) / difarControl.getDifarParameters().nominalSpreading;
			return Math.pow(10., f);
		case DifarParameters.PROPLOSS_CYLINDRICAL:
			double sphericalLoss = 10*Math.log10(difarControl.getDifarParameters().cylindricalStartDistance);
			f = (nominalSourceLevel - rl - sphericalLoss) / 10;
			return Math.pow(10., f);
		case DifarParameters.PROPLOSS_NONE:
		default:
			return difarControl.getDifarParameters().defaultLength;
		}
	}
	
	PamWarning noCalibrationGps = new PamWarning("DIFAR Processing", "No GPS fix for compass calibration source location", 1);
	private double getVesselRange(DifarDataUnit difarDataUnit) {
		// see if it's possible to work out where the vessel is at this point in time
		// and return the range from the data unit to the vessel even if it's in the wrong direction. 
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return difarControl.getDifarParameters().getDefaultRange();
		}
		String gpsSourceName =  difarControl.getDifarParameters().calibrationGpsSource;
		if (!gpsControl.getGpsDataBlock().getLongDataName().equalsIgnoreCase(gpsSourceName)){
			gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType, gpsSourceName);
			if (gpsControl == null) {
				return difarControl.getDifarParameters().getDefaultRange();
			}
		}
		GpsDataUnit shipGpsUnit = gpsControl.getShipPosition(difarDataUnit.getTimeMilliseconds());
		
		if (shipGpsUnit == null) {
			WarningSystem.getWarningSystem().addWarning(noCalibrationGps);
			return difarControl.getDifarParameters().getDefaultRange();
		}
		if (Math.abs(difarDataUnit.getTimeMilliseconds() - shipGpsUnit.getTimeMilliseconds()) > 180*1000) {
			
			if (PamCalendar.getTimeInMillis() - difarDataUnit.getTimeMilliseconds() < 300*1000){ 
				WarningSystem.getWarningSystem().addWarning(noCalibrationGps);
			}
			return difarControl.getDifarParameters().getDefaultRange();
		}
		LatLong oll = difarDataUnit.getOriginLatLong(false);
		if (oll == null) {
			WarningSystem.getWarningSystem().addWarning(noCalibrationGps);
			return difarControl.getDifarParameters().getDefaultRange();
		}
		return Math.max(oll.distanceToMetres(shipGpsUnit.getGpsData()),
				difarControl.getDifarParameters().getDefaultRange());
	}
	/**
	 * Get the display FFT length, which may depend on the type of data unit. 
	 * @return FFT length for data display. 
	 */
	public int getDisplayFFTLength(DifarDataUnit difarDataUnit) {
		int fftLen;
		// Get FFT length from DIFAR parameters (whale or vessel), NOT the clip display.
		if (difarDataUnit.isVessel()){
			fftLen = difarControl.getDifarParameters().findSpeciesParams(DifarParameters.CalibrationClip).FFTLength;
		} else {
			fftLen = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit.getLutSpeciesItem().toString()).FFTLength;
		}
			
		if (fftLen < 2) {
			return 256;
		}
		return fftLen;
	}
	
	public int getDisplayFFTHop(DifarDataUnit difarDataUnit) {
		int fftHop;
		
		// Get FFT length from DIFAR parameters (whale or vessel), NOT the clip display.
		if (difarDataUnit.isVessel()){
			fftHop = difarControl.getDifarParameters().findSpeciesParams(DifarParameters.CalibrationClip).FFTHop;
		} else {
			fftHop = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit.getLutSpeciesItem().toString()).FFTHop;
		}
		if (fftHop < 0) {
			return 1;
		}
		return fftHop;
	}
	
	/**
	 * Get the correction that would be needed to make this difar unit point straight at
	 * the vessel. 
	 * @param difarDataUnit
	 * @return correction in degrees
	 */
	private Double getVesselCorrection(DifarDataUnit difarDataUnit) {
		GPSControl gpsControl = null;
		
		// Try loading the desired GPS data stream (additional GPS processing can be added in Viewer mode)
		String gpsSourceName =  difarControl.getDifarParameters().calibrationGpsSource;
		gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.class, gpsSourceName);
		
		// Desired GPS stream has changed or become unavailable, so use the default
		if (gpsControl == null) {
			gpsControl = GPSControl.getGpsControl();
		}
		
		// Can't get a vessel correction if we don't have a sound source location
		if (gpsControl == null) {
			return null;
		}

		GpsDataUnit shipGpsUnit = gpsControl.getShipPosition(difarDataUnit.getTimeMilliseconds());
		if (shipGpsUnit == null) {
			return null;
		}
		GpsData shipGps = shipGpsUnit.getGpsData();
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
		// convert a channel to a hydrophones. 
		int phoneNumber = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		PamProcess sourceProcess = this.getSourceProcess();
		/**
		 * Commented out the four lines below because they threw an exception when using
		 * the instrument inputs on my Fireface UFX. These instrument inputs are numbered
		 * channels 8-11 (zero indexed) as an ASIO USB sound card. Commenting this code
		 * fixes my issues with vessel calibration clips on my UFX, but I'm not sure how
		 * it will affect vessel calibrations with other sound cards.
		 */
//		if (AcquisitionProcess.class.isAssignableFrom(sourceProcess.getClass())) {
//			AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess;
//			phoneNumber = daqProcess.getAcquisitionControl().acquisitionParameters.getChannelListIndexes(phoneNumber);
//		}
		LatLong hLatLong = difarDataUnit.getOriginLatLong(false);
		if (hLatLong == null) {
			return null;
		}
		/*
		 * The correction this clip suggests is on top of whatever correction the
		 * buoy already has, and the caller adds the two together. So the heading
		 * used here has to be the same one the caller uses, which is the buoy
		 * record's. Reading it from the core array instead gave a different
		 * number, and the difference landed straight in the stored heading.
		 */
		double arrayHead = difarControl.sonobuoyManager.getCompassCorrection(phoneNumber,
				difarDataUnit.getTimeMilliseconds());
		double bearing = hLatLong.bearingTo(shipGps);
		double bearingCorr = bearing - (difarDataUnit.getSelectedAngle() + arrayHead);
		bearingCorr = PamUtils.constrainedAngle(bearingCorr, 180);
		return bearingCorr;
	}

	/**
	 * Called when a vessel data unit is about to be saved so that we can work out where the
	 * vessel was at that time and apply an update to the streamer orientation. 
	 * @param difarDataUnit data unit. 
	 */
	@Deprecated
	private boolean updateDifarAngle(DifarDataUnit difarDataUnit) {
		Double bearingCorr = getVesselCorrection(difarDataUnit);
		if (bearingCorr == null) {
			return false;
		}

		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
//		// convert a channel to a hydrophones. 
		int phoneNumber = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		double arrayHead = difarDataUnit.getHydrophoneHeading(false);
		double newArrayHead = arrayHead + bearingCorr;
		int streamerIndex = currentArray.getStreamerForPhone(phoneNumber);
		Streamer streamer = currentArray.getStreamer(streamerIndex);
		streamer.setHeading(newArrayHead);
		OriginSettings os = streamer.getOriginSettings();
		if (StaticOriginSettings.class.isAssignableFrom(os.getClass())) {
			StaticOriginSettings sos = (StaticOriginSettings) os;
			sos.getStaticPosition().getGpsData().setTrueHeading(newArrayHead);
			currentArray.newStreamerFromUpdate(difarDataUnit.getTimeMilliseconds(), streamerIndex, null, null, null, sos, null);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Get the calibration sub-process for a specific channel. 
	 * @param channel
	 * @return calibration process. 
	 */
	synchronized public CalibrationProcess getCalibrationProcess(int channel) {
		if (calibrationProcesses[channel] == null) {
			calibrationProcesses[channel] = new CalibrationProcess(difarControl, channel);
		}
		return calibrationProcesses[channel];
	}
	
	/**
	 * Get the calibration true bearing histogram for a channel. 
	 * @param channel channel number (0 - 31)
	 * @return calibration histogram.
	 */
	synchronized public CalibrationHistogram getCalTrueBearingHistogram(int channel) {
		if (calTrueBearingHistograms[channel] == null) {
			calTrueBearingHistograms[channel] = new CalibrationHistogram(difarControl, channel, 360);
			calTrueBearingHistograms[channel].setName("Uncorrected Bearing");
		}
		return calTrueBearingHistograms[channel];
	}
	/**
	 * Get the calibration true bearing histogram for a channel. 
	 * @param channel channel number (0 - 31)
	 * @return calibration histogram.
	 */
	synchronized public CalibrationHistogram getCalCorrectionHistogram(int channel) {
		if (calCorrectionHistograms[channel] == null) {
			calCorrectionHistograms[channel] = new CalibrationHistogram(difarControl, channel, 180);
			// each clip's correction is added to the heading the buoy already has
			calCorrectionHistograms[channel].setName("Change to buoy heading");
		}
		return calCorrectionHistograms[channel];
	}

	
	class TriggerObserver extends PamObserverAdapter {

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			for (int i=0; i < triggerDataSources.size(); i++){
				if (o == triggerDataSources.get(i)) {
					newTrigger(o, arg);
				}
			}
		}

		@Override
		public String getObserverName() {
			return null;
		}

	}


	public void cancelAutoSaveTimer() {
		if (getAutoSaveTimer() != null) {
			getAutoSaveTimer().stop();
			setAutoSaveTimer(null);
		}
	}

	public void setAutoSaveTimer(AutoSaveTimer autoSaveTimer) {
		this.autoSaveTimer = autoSaveTimer;
	}

	public CalibrationDataBlock getCalibrationDataBlock() {
		return calibrationDataBlock;
	}

	public void setCalibrationDataBlock(CalibrationDataBlock calibrationData) {
		this.calibrationDataBlock = calibrationData;
	}
		
}
