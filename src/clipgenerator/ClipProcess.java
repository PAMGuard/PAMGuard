package clipgenerator;

import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sound.sampled.AudioFormat;

import networkTransfer.receive.BuoyStatusDataUnit;
import soundPlayback.ClipPlayback;
import warnings.PamWarning;
import warnings.WarningSystem;
import clipgenerator.clipDisplay.ClipSymbolManager;
import clipgenerator.localisation.ClipDelays;
import clipgenerator.localisation.ClipLocalisation;
import dataPlotsFX.layout.TDGraphFX;
import fftManager.FFTDataBlock;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import PamController.PamController;
import PamUtils.FileFunctions;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkProcess;
import annotation.handler.ManualAnnotationHandler;

/**
 * Process for making short clips of audio data. 
 * <br> separate subscriber processes for each triggering data block, but these all send clip requests
 * back into the main observer of the actual raw data - so that all clips are made from the 
 * same central thread. 
 * <br> Let the request queue trigger off the main clock signal. 
 *  
 * @author Doug Gillespie
 *
 */
public class ClipProcess extends SpectrogramMarkProcess {

	private ClipControl clipControl;
	
	private PamDataBlock[] dataSources;

	private ClipBlockProcess[] clipBlockProcesses;
	
	private List<ClipRequest> clipRequestQueue;
	
	private Object clipRequestSynch = new Object();
	
	private PamRawDataBlock rawDataBlock;
	
	private ClipDataBlock clipDataBlock;

	private long specMouseDowntime;

	private boolean specMouseDown;

	private long masterClockTime;
	
	private ClipDelays clipDelays;
	
	private BuoyLocaliserManager buoyLocaliserManager;
	
	private ClipSpectrogramMarkDataBlock clipSpectrogramMarkDataBlock;

	private ManualAnnotationHandler manualAnnotaionHandler;
	
	private static PamWarning warningMessage = new PamWarning("Clip Generator", "", 2);

	public ClipProcess(ClipControl clipControl) {
		super(clipControl);
		this.clipControl = clipControl;
		clipRequestQueue = new LinkedList<ClipRequest>();
		clipSpectrogramMarkDataBlock = new ClipSpectrogramMarkDataBlock(this, 0);
		clipDataBlock = new ClipDataBlock(clipControl.getUnitName() + " Clips", this, 0);
		clipDataBlock.setBinaryDataSource(new ClipBinaryDataSource(clipControl, clipDataBlock));
		ClipOverlayGraphics cog = new ClipOverlayGraphics(clipControl, clipDataBlock);
		clipDataBlock.setOverlayDraw(cog);
		StandardSymbolManager symbolManager = new ClipSymbolManager(clipDataBlock, ClipOverlayGraphics.defSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		clipDataBlock.setPamSymbolManager(symbolManager);
		addOutputDataBlock(clipDataBlock);
		clipDelays = new ClipDelays(clipControl);
		buoyLocaliserManager = new BuoyLocaliserManager();
		manualAnnotaionHandler = new ManualAnnotationHandler(clipControl, clipDataBlock);
		clipDataBlock.setAnnotationHandler(manualAnnotaionHandler);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		super.newData(o, arg);
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL &&
				PamController.getInstance().getRunMode() != PamController.RUN_MIXEDMODE) {
			return;
		}
		processRequestList();
	}

	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		masterClockTime = timeMilliseconds;
	}

	/**
	 * Process the queue of clip request - these are passed straight back
	 * into the ClipBlockProcesses which started them since there is a 
	 * certain amount of bookkeeping which needs to be done at the
	 * individual block level.  
	 */
	private void processRequestList() {
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL &&
				PamController.getInstance().getRunMode() != PamController.RUN_MIXEDMODE) {
			return;
		}
		if (clipRequestQueue.size() == 0) {
			return;
		}
		synchronized(clipRequestSynch) {
			ClipRequest clipRequest;
			ListIterator<ClipRequest> li = clipRequestQueue.listIterator();
			int clipErr;
			while (li.hasNext()) {
				clipRequest = li.next();
				clipErr = clipRequest.clipBlockProcess.processClipRequest(clipRequest);
				switch (clipErr) {
				case 0: // no error - clip should have been created. 
				case RawDataUnavailableException.DATA_ALREADY_DISCARDED:
				case RawDataUnavailableException.INVALID_CHANNEL_LIST:
					//					System.out.println("Clip error : " + clipErr);
					li.remove();
				case RawDataUnavailableException.DATA_NOT_ARRIVED:
					continue; // hopefully, will get this next time !
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		/*
		 * Work out which hydrophones are in use and create an appropriate bearing
		 * localiser. 
		 */
		subscribeDataBlocks();
	}

	@Override
	public void pamStart() {
		super.pamStart();
		clipRequestQueue.clear(); // just in case anything hanging around from previously. 
		// if there is it may crash since the ClipblockProcess will probably have been replaced anyway. 
	}

	/**
	 * Find the wav file to go with a particular clip
	 * @param clipDataUnit data unit to find the file for. 
	 * @return file, or null if not found. 
	 */
	public File findClipFile(ClipDataUnit clipDataUnit) {
		String path = getClipFileFolder(clipDataUnit.getTimeMilliseconds(), true);
		path += clipDataUnit.fileName;
		File aFile = new File(path);
		if (aFile.exists() == false) {
			return null;
		}
		return aFile;
	}
	
	/**
	 * Get the output folder, based on time and sub folder options. 
	 * @param timeStamp
	 * @param addSeparator
	 * @return
	 */
	private String getClipFileFolder(long timeStamp, boolean addSeparator) {
		String fileSep = FileParts.getFileSeparator();
		if (clipControl.clipSettings.outputFolder == null) return null;
		String folderName = new String(clipControl.clipSettings.outputFolder);
		if (clipControl.clipSettings.datedSubFolders) {
			folderName += fileSep + PamCalendar.formatFileDate(timeStamp);

			// now check that that folder exists. 
			File folder = FileFunctions.createNonIndexedFolder(folderName);
			if (folder == null || folder.exists() == false) {
				return null;
			}
		}
		if (addSeparator) {
			folderName += fileSep;
		}
		return folderName;
	}

	@Override
	public boolean flushDataBlockBuffers(long maxWait) {
		boolean ans = super.flushDataBlockBuffers(maxWait);
		processRequestList(); // one last go at processing the clip request list before stopping.
		
		return ans;
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		long minH = 0;
		if (clipBlockProcesses == null || clipBlockProcesses.length == 0) {
			return 0;
		}
		for (int i = 0; i < clipBlockProcesses.length; i++) {
			if (clipBlockProcesses[i] == null) {
				continue;
			}
			minH = Math.max(minH, clipBlockProcesses[i].getRequiredDataHistory(o, arg));
		}
		minH += Math.max(3000, 192000/(long)getSampleRate());
		if (specMouseDown) {
			minH = Math.max(minH, masterClockTime-specMouseDowntime);
		}
		return minH;
	}

	private void addClipRequest(ClipRequest clipRequest) {
		synchronized (clipRequestSynch) {
			clipRequestQueue.add(clipRequest);
		}
	}
	
	@Override
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, int downUp, int channel, 
			long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay) {
		/**
		 * Called when a manual mark is made on the spectrogram display. 
		 */
		if (downUp == SpectrogramMarkProcess.MOUSE_DOWN) {
			// REMOVE THIS CHECK - ClipGenerator already knows the raw data source, set in the parameters.  So it doesn't need to worry about whether the FFT source is actually beamformer data
//    		// do a quick check here of the source.  If the fft has sequence numbers, the channels are ambiguous and Rocca can't use it.  warn the user and exit
//    		FFTDataBlock source = display.getSourceFFTDataBlock();
//    		if (source.getSequenceMapObject()!=null) {
//    			String err = "Error: this Spectrogram uses Beamformer data as it's source, and Beamformer output does not contain "
//    			+ "the link back to a single channel of raw audio data that the Clip Generator requires.  You will not be able to select audio clips "
//    			+ "until the source is changed";
//    			warningMessage.setWarningMessage(err);
//    			WarningSystem.getWarningSystem().addWarning(warningMessage);
//    			return false;
//    		} else {
//    			WarningSystem.getWarningSystem().removeWarning(warningMessage);
//    		}

			specMouseDown = true;
			specMouseDowntime = startMilliseconds;
			return false;
		}
		else if (downUp == SpectrogramMarkProcess.MOUSE_DRAG) {
			return false;
		}
		else {
			specMouseDown = false;
		}
		if (duration == 0) {
			// no duration or extent in time. Mouse was probably just clicked on spectrogram
			// so don't bother. 
			return false;
		}
		if (rawDataBlock == null) {
			return false;
		}
		long startSample = absMillisecondsToSamples(startMilliseconds);
		int numSamples = (int) relMillisecondsToSamples(duration);
		int channelMap;
		channelMap = PamUtils.SetBit(0, channel, 1); // just the channel that had the mark
		channelMap = rawDataBlock.getChannelMap(); // all channels in the raw data block 
		double[][] rawData = null;
		try {
			rawData = rawDataBlock.getSamples(startSample, numSamples, channelMap);
		} catch (RawDataUnavailableException e) {
			System.out.println("RawDataUnavailableException in ClipProcess.spectrogramNotification  :" + e.getMessage());
			return false;
		}
		if (rawData == null) {
			return false;
		}
		/**
		 * Just make a data unit and send off in the same way as
		 * we would have for detection data units
		 */
		double[] freq = {Math.min(f1, f2), Math.max(f1, f2)};
		ClipSpectrogramMark clipMark = new ClipSpectrogramMark(startMilliseconds, 1<<channel, startSample, numSamples, freq);
		clipSpectrogramMarkDataBlock.addPamData(clipMark);
//		ClipDataUnit clipDataUnit;
//		if (clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
//			String folderName = getClipFileFolder(startMilliseconds, true);
//			String fileName = PamCalendar.createFileName(startMilliseconds, "Clip", ".wav");
//			WavFile wavFile = new WavFile(folderName+fileName, "w");
//			wavFile.write(getSampleRate(), rawData.length, rawData);
//			// make a data unit to go with it. 
//			clipDataUnit = new ClipDataUnit(startMilliseconds, startMilliseconds, startSample,
//					(int)(numSamples), channelMap, fileName, "Manual Clip", null, getSampleRate());
//		}
//		else {
//			clipDataUnit = new ClipDataUnit(startMilliseconds, startMilliseconds, startSample,
//					(int)(numSamples), channelMap, "", "Manual Clip", rawData, getSampleRate());
//		}
//		clipDataUnit.setFrequency(freq);
//		localiseClip(clipDataUnit);
//		clipDataBlock.addPamData(clipDataUnit);
		return false;
	}

	/*
	 * Localise the given clip. 
	 * 
	 */
	private boolean localiseClip(ClipDataUnit clipDataUnit) {
		/**
		 * First need to find an appropriate bearing localiser. 
		 * It's possible that it's come with one in which case
		 * that's great, if not, then it will need to use the standard one.
		 * Will have to remake it if it's for a different hydrophone list.  
		 */
		// first work out which hydrophones are used by the clipDataUnit. 
		// if they are the same as last time, then we're cool. 
		int hydros = rawDataBlock.getChannelListManager().channelIndexesToPhones(clipDataUnit.getChannelBitmap());
		// give up immediately if there is only one hydrophone
		if (PamUtils.getNumChannels(hydros) < 2) {
			return false;
		}
		BearingLocaliser bearingLocaliser = buoyLocaliserManager.findBearingLocaliser(clipDataUnit);

		return localiseClip(clipDataUnit, bearingLocaliser, hydros);
	}
	/**
	 * Localise a clip. Calculate the delays based on cross correlation
	 * then convert to angles using an appropriate bearing localiser. 
	 * <p>Note that this currently only works for closely spaced hydrophones 
	 * where the max delay is < half the FFT length. 
	 * @param clipDataUnit data unit to localise
	 * @param bearingLocaliser bearing localiser converts delays to angle(s)
	 * @param hydrophoneMap hydrophone map
	 * @return true if a localisation was calculated. 
	 */
	private boolean localiseClip(ClipDataUnit clipDataUnit, BearingLocaliser bearingLocaliser, int hydrophoneMap) {
		double[] delays = clipDelays.getDelays(clipDataUnit);
		if (delays != null) {
			for (int i = 0; i < delays.length; i++) {
				delays[i] /= rawDataBlock.getSampleRate();
			}
			double[][] locData = bearingLocaliser.localise(delays, clipDataUnit.getTimeMilliseconds());
			if (locData != null) {
				ClipLocalisation clipLoc = new ClipLocalisation(clipDataUnit, bearingLocaliser, hydrophoneMap, locData);
				clipDataUnit.setLocalisation(clipLoc);
			}
		}
		return (clipDataUnit.getLocalisation() != null);
	}

	@Override
	public boolean canMark() {
		return (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW);
	}
	
	/**
	 * Called at end of setup of after settings dialog to subscribe data blocks. 
	 */
	public synchronized void subscribeDataBlocks() {
		unSubscribeDataBlocks();
		rawDataBlock = PamController.getInstance().getRawDataBlock(clipControl.clipSettings.dataSourceName);
		setParentDataBlock(rawDataBlock, true);
		
		int nBlocks = clipControl.clipSettings.getNumClipGenerators();
		clipBlockProcesses = new ClipBlockProcess[nBlocks];
		PamDataBlock aDataBlock;
		ClipGenSetting clipGenSetting;
		for (int i = 0; i < nBlocks; i++) {
			
			clipGenSetting = clipControl.clipSettings.getClipGenSetting(i);

			if (clipGenSetting.enable == false) {
				continue;
			}
			if (i == 0) {
				aDataBlock = this.clipSpectrogramMarkDataBlock;
			}
			else {
				aDataBlock = PamController.getInstance().getDetectorDataBlock(clipGenSetting.dataName); 

			}
			if (aDataBlock == null) {
				continue;
			}
			clipBlockProcesses[i] = new ClipBlockProcess(this, aDataBlock, clipGenSetting);
		}
	}
	
	/**
	 * Kill off the old ClipBlockProcesses before creating new ones. 
	 */
	private void unSubscribeDataBlocks() {
		if (clipBlockProcesses == null) {
			return;
		}
		for (int i = 0; i < clipBlockProcesses.length; i++) {
			if (clipBlockProcesses[i] == null) {
				continue;
			}
			clipBlockProcesses[i].disconnect();
		}
	}
	
	public class ClipBlockProcess extends PamObserverAdapter {
		
		private PamDataBlock dataBlock;
		
		protected ClipGenSetting clipGenSetting;

		protected ClipProcess clipProcess;

		private ClipDataUnit lastClipDataUnit;

		private WavFileWriter wavFile;
		
		private StandardClipBudgetMaker clipBudgetMaker;

		private BearingLocaliser bearingLocaliser;

		private int hydrophoneMap;
		
		/**
		 * @param dataBlock
		 * @param clipGenSetting
		 */
		public ClipBlockProcess(ClipProcess clipProcess, PamDataBlock dataBlock,
				ClipGenSetting clipGenSetting) {
			super();
			this.clipProcess = clipProcess;
			this.dataBlock = dataBlock;
			this.clipGenSetting = clipGenSetting;
			clipBudgetMaker = new StandardClipBudgetMaker(this);
			dataBlock.addObserver(this, true);
			

			if (rawDataBlock != null) {
				int chanMap = decideChannelMap(rawDataBlock.getChannelMap());
				hydrophoneMap = rawDataBlock.getChannelListManager().channelIndexesToPhones(chanMap);
				double timingError = Correlations.defaultTimingError(getSampleRate());
				bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(hydrophoneMap, timingError); 
			}
		}
		
		/**
		 * Process a clip request, i.e. make an actual clip from the raw data. This is called back 
		 * from the main thread receiving raw audio data and is called only after any decisions regarding
		 * whether or not a clip should be made have been taken - to get on and make the clip in the
		 * output folder. 
		 * @param clipRequest clip request information
		 * @return 0 if OK or the cause from RawDataUnavailableException if data are not available. 
		 */
		private int processClipRequest(ClipRequest clipRequest) {
//			System.out.println("Process clip request:? " +clipRequest.dataUnit); 
			PamDataUnit dataUnit = (PamDataUnit) clipRequest.dataUnit;
			long rawStart = dataUnit.getStartSample();
			long rawEnd = rawStart + dataUnit.getSampleDuration();
//			rawStart -= (clipGenSetting.preSeconds * getSampleRate());
			rawStart = (long) Math.max(rawStart-clipGenSetting.preSeconds * getSampleRate(),0); // prevent negative numbers, just start at the beginning if detection is near start of file
			rawEnd += (clipGenSetting.postSeconds * getSampleRate());
			int channelMap = decideChannelMap(dataUnit.getChannelBitmap());
			
			boolean append = false;
//			if (lastClipDataUnit != null) {
//				if (rawStart < (lastClipDataUnit.getStartSample()+lastClipDataUnit.getSampleDuration()) &&
//						channelMap == lastClipDataUnit.getChannelBitmap()) {
//					append = true;
//					rawStart = lastClipDataUnit.getStartSample()+lastClipDataUnit.getSampleDuration();
//					if (rawEnd < rawStart) {
//						return 0; // nothing to do !
//					}
//				}
//			}
			
			double[][] rawData = null;
			try {
				rawData = rawDataBlock.getSamples(rawStart, (int) (rawEnd-rawStart), channelMap);
			}
			catch (RawDataUnavailableException e) {
				return e.getDataCause();
			}
			if (rawData==null) {
				System.out.println("Null raw data");
				return RawDataUnavailableException.DATA_ALREADY_DISCARDED; // if rawDataBlock.getSamples returns null, assume that the data is already gone and return an error
			}
			if (append && clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
				wavFile.append(rawData);
				lastClipDataUnit.setSampleDuration(rawEnd-lastClipDataUnit.getStartSample());
				clipDataBlock.updatePamData(lastClipDataUnit, dataUnit.getTimeMilliseconds());
//				System.out.println(String.format("%d samples added to file", rawData[0].length));
			}
			else {
				ClipDataUnit clipDataUnit = null;
				long startMillis = dataUnit.getTimeMilliseconds() - (long) (clipGenSetting.preSeconds*1000.);
				String fileName = "";
				if ((clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) 
						|| (clipControl.clipSettings.storageOption == ClipSettings.STORE_BOTH)) {
					String folderName = getClipFileFolder(dataUnit.getTimeMilliseconds(), true);
					fileName = getClipFileName(startMillis);
					AudioFormat af = new Wav16AudioFormat(getSampleRate(), rawData.length);
					wavFile = new WavFileWriter(folderName+fileName, af);
					wavFile.write(rawData);
					wavFile.close();
					// make a data unit to go with it. 
					clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
							(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
				}
				if ((clipControl.clipSettings.storageOption == ClipSettings.STORE_BINARY) 
						|| (clipControl.clipSettings.storageOption == ClipSettings.STORE_BOTH)) {
					clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
							(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
				}
				clipDataUnit.setTriggerDataUnit(dataUnit);
				clipDataUnit.setFrequency(dataUnit.getFrequency());
				lastClipDataUnit = clipDataUnit;
				if (bearingLocaliser != null) {
					localiseClip(clipDataUnit, bearingLocaliser, hydrophoneMap);
				}				
				clipDataBlock.addPamData(clipDataUnit);
			}
			
			return 0; // no error. 
		}

		private String getClipFileName(long timeStamp) {
			return PamCalendar.createFileNameMillis(timeStamp, clipGenSetting.clipPrefix, ".wav");
		}
	

		/**
		 * Decide which channels should actually be used. 
		 * @param channelBitmap
		 * @return
		 */
		protected int decideChannelMap(int channelBitmap) {
			switch (clipGenSetting.channelSelection) {
			case ClipGenSetting.ALL_CHANNELS:
				return rawDataBlock.getChannelMap();
			case ClipGenSetting.DETECTION_CHANNELS_ONLY:
				return channelBitmap;
			case ClipGenSetting.FIRST_DETECTION_CHANNEL_ONLY:
				int overlap = channelBitmap & rawDataBlock.getChannelMap();
				int first = PamUtils.getLowestChannel(overlap);
				return 1<<first;
			}
			return 0;
		}

		/**
		 * disconnect from it's data source. 
		 */
		public void disconnect() {
			dataBlock.deleteObserver(this);
		}
		
		@Override
		public String getObserverName() {
			return clipProcess.getObserverName();
		}
		@Override
		public PamObserver getObserverObject() {
			return clipProcess.getObserverObject();
		}
		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) ((clipGenSetting.preSeconds+clipGenSetting.postSeconds) * 1000.);
		}
		
		
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			/**
			 * This one should get updates from the triggering data block. 
			 */
//			System.out.printf("Clip request: " + dataUnit.toString());
			if (shouldMakeClip((PamDataUnit) dataUnit)) {
//				System.out.printf(": Clip requested\n");
				addClipRequest(new ClipRequest(this, dataUnit));
			}
//			else {
//				System.out.printf(": Clip request refused\n");
//			}
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * Function to decide whether or not a clip should be made. 
		 * Might be set to all clips, might be working to a budget. 
		 * Will ultimately be calling into quite a long winded decision
		 * making process. 
		 * @param arg
		 * @return true if a clip should be made, false otherwsie. 
		 */
		private boolean shouldMakeClip(PamDataUnit dataUnit) {
			return clipBudgetMaker.shouldStore(dataUnit);
		}
		
	}
	/**
	 * Data needed for a clip request. 
	 * @author Doug Gillespie
	 *
	 */
	public class ClipRequest {

		/**
		 * @param clipBlockProcess
		 * @param dataUnit
		 */
		public ClipRequest(ClipBlockProcess clipBlockProcess,
				PamDataUnit dataUnit) {
			super();
			this.clipBlockProcess = clipBlockProcess;
			this.dataUnit = dataUnit;
		}

		protected ClipBlockProcess clipBlockProcess;
		
		protected PamDataUnit dataUnit;

	}
	
	/**
	 * @return the clipControl
	 */
	public ClipControl getClipControl() {
		return clipControl;
	}

	/**
	 * @return the clipDataBlock
	 */
	public ClipDataBlock getClipDataBlock() {
		return clipDataBlock;
	}

	/**
	 * @return the clipSpectrogramMarkDataBlock
	 */
	public ClipSpectrogramMarkDataBlock getClipSpectrogramMarkDataBlock() {
		return clipSpectrogramMarkDataBlock;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#processNewBuoyData(networkTransfer.receive.BuoyStatusDataUnit, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void processNewBuoyData(BuoyStatusDataUnit buoyStatusDataUnit,
			PamDataUnit pamDataUnit) {
		/*
		 * Attempt to localise the clip. 
		 * localise function will automatically select the bearing localiser
		 * for this group of hydrophones.  
		 */
		ClipDataUnit clipDataUnit = (ClipDataUnit) pamDataUnit;
		localiseClip(clipDataUnit);
	}
	
	/**
	 * Serve up different bearing localisers depending on which channels 
	 * are being used. 
	 * @author Doug Gillespie
	 *
	 */
	class BuoyLocaliserManager {
		private ArrayList<BearingLocaliser> bearingLocalisers = new ArrayList<>();
		
		/**
		 * Find a bearing localiser that has the same hydrophones. 
		 * @param pamDataUnit data unit
		 * @return bearing localiser. Will create if necessary. 
		 */
		public synchronized BearingLocaliser findBearingLocaliser(PamDataUnit pamDataUnit) {
			int channels = pamDataUnit.getChannelBitmap();
			int hydros = rawDataBlock.getChannelListManager().channelIndexesToPhones(channels);
			for (BearingLocaliser loc:bearingLocalisers) {
				if (loc.getHydrophoneMap() == hydros) {
					return loc;
				}
			}
			// if it get's here, then there isn't one available so create one
			double timingError = Correlations.defaultTimingError(getSampleRate());
			BearingLocaliser loc = BearingLocaliserSelector.createBearingLocaliser(hydros, timingError);
			if (loc != null) {
				bearingLocalisers.add(loc);
			}
			return loc;
		}
		
		/**
		 * Clear all bearing localisers from the list. 
		 */
		public synchronized void clearAll() {
			bearingLocalisers.clear();
		}
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void playClip(ClipDataUnit clipDataUnit) {
		if (clipDataUnit == null) return;
		ClipPlayback.getInstance().playClip(clipDataUnit.getRawData(), clipDataUnit.getParentDataBlock().getSampleRate(), true);
		
	}

	/**
	 * @return the manualAnnotaionHandler
	 */
	public ManualAnnotationHandler getManualAnnotaionHandler() {
		return manualAnnotaionHandler;
	}
}
