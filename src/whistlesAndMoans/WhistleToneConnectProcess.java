package whistlesAndMoans;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import networkTransfer.receive.BuoyStatusDataUnit;
import spectrogramNoiseReduction.SpectrogramNoiseProcess;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;
import whistlesAndMoans.plots.WhistleSymbolManager;
import eventCounter.DataCounter;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.SQLLogging;
import Array.ArrayManager;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.annotation.BearingLocAnnotationType;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import PamDetection.LocContents;
import PamModel.SMRUEnable;
import PamUtils.CPUMonitor;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.background.SpecBackgroundDataUnit;
import PamguardMVC.background.SpecBackgroundManager;
import Spectrogram.SpectrumBackgrounds;

public class WhistleToneConnectProcess extends PamProcess {
	
	public static final String streamName = "Contours";

	private WhistleMoanControl whistleMoanControl;

	/**
	 * This will be the source data for the noise reduction and threshold process. 
	 */
	private FFTDataBlock sourceData;

	private ConnectedRegionDataBlock outputData;

	private PamDataBlock<WhistleToneGroupedDetection> whistleLocations;

	private ShapeConnector[] shapeConnectors;

	protected DataCounter dataCounter;

	/**
	 * total number of shape connectors. 
	 */
	private int nConnectors;

	private WhistleDetectionGrouper detectionGrouper;

	private DetectionGroupLocaliser detectionGroupLocaliser;

	private WhistleToneConnectProcess THAT;

	private WhistleToneLogging whistleToneLogging;

	private static final int NSUMMARYPOINTS = 4;

	private int summaryBlockSize = 1;

	private int[] whistleSummaryCount = new int[NSUMMARYPOINTS];

	private WMRecorderTrigger wmRecorderTrigger;

	private int chanOrSeqMap;

	private BearingLocAnnotationType bearingLocAnnotationType;

	private CPUMonitor localisationCPU = new CPUMonitor();
	
	private StubRemover stubRemover;
	
	private SpectrumBackgrounds spectrumBackgrounds;
	
	private SpectrumBackgroundObserver spectrumBackgroundObserver;
	
	private SpecBackgroundManager specBackgroundManager;
	
	private long lastBackgroundTime = 0;

	public WhistleToneConnectProcess(WhistleMoanControl whitesWhistleControl) {
		super(whitesWhistleControl, null);
		this.whistleMoanControl = whitesWhistleControl;
		THAT = this;

//		outputData = new ConnectedRegionDataBlock(whistleMoanControl.getUnitName() + " Contours", whistleMoanControl, this, 0);
		outputData = new ConnectedRegionDataBlock("Contours", whistleMoanControl, this, 0);
		addOutputDataBlock(outputData);
		outputData.setOverlayDraw(new CROverlayGraphics(outputData, whitesWhistleControl));
		//		outputData.setNaturalLifetime(5);
		outputData.SetLogging(whistleToneLogging = new WhistleToneLogging(whitesWhistleControl, outputData, SQLLogging.UPDATE_POLICY_WRITENEW));
		outputData.setBinaryDataSource(new WhistleBinaryDataSource(this, outputData, streamName));
		outputData.setJSONDataSource(new WhistleJSONDataSource());
		outputData.setDatagramProvider(new WMDatagramProvider(outputData));
		outputData.setCanClipGenerate(true);
		StandardSymbolManager symbolManager = new WhistleSymbolManager(outputData, CROverlayGraphics.defaultSymbol.clone(), true);
		outputData.setPamSymbolManager(symbolManager);
		//		addBinaryDataSource(outputData.getSisterBinaryDataSource());

		detectionGrouper = new WhistleDetectionGrouper(whitesWhistleControl, outputData);
		
		stubRemover = new StubRemover(whitesWhistleControl);

//		whistleLocations = new WhistleLocationDataBlock("Localised " + whistleMoanControl.getUnitName() +  " Contours", this, 
		whistleLocations = new WhistleLocationDataBlock("Localised Contours", this, 
				whitesWhistleControl.whistleToneParameters.getChanOrSeqBitmap());	// Note the channelMap in WhistleToneParameters object may be a sequence map or a channel map, depending on source
		whistleLocations.setOverlayDraw(new WhistleToneLocalisationGraphics(whistleLocations));
		whistleLocations.setPamSymbolManager(new WhistleSymbolManager(whistleLocations, WhistleToneLocalisationGraphics.defaultSymbol.clone(), true));
		addOutputDataBlock(whistleLocations);
		whistleLocations.getLocalisationContents().setLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE |
				LocContents.HAS_LATLONG | LocContents.HAS_PERPENDICULARERRORS| LocContents.HAS_AMBIGUITY);
		whistleLocations.SetLogging(new PamDetectionLogging(whistleLocations, SQLLogging.UPDATE_POLICY_WRITENEW));

		detectionGroupLocaliser = new DetectionGroupLocaliser(this);

		dataCounter = new DataCounter(whitesWhistleControl.getUnitName(), outputData, 60);
		dataCounter.setEventTrigger(60, 10);
		dataCounter.setShortName("");

		wmRecorderTrigger = new WMRecorderTrigger(whitesWhistleControl, outputData);
		outputData.setRecordingTrigger(wmRecorderTrigger);

		bearingLocAnnotationType = new BearingLocAnnotationType();
		
		specBackgroundManager = new SpecBackgroundManager(this, outputData);
		outputData.setBackgroundManager(specBackgroundManager);
	}


	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		//		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());	// use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
		int chan = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
		for (int i = 0; i < nConnectors; i++) {
			if (shapeConnectors[i].firstChannel == chan) {
				shapeConnectors[i].newData(fftDataUnit.getFftSlice(), fftDataUnit);
			}
		}
		if (fftDataUnit.getTimeMilliseconds() >= lastBackgroundTime + whistleMoanControl.getWhistleToneParameters().getBackgroundInterval() * 1000.) {
			lastBackgroundTime = fftDataUnit.getTimeMilliseconds();
			long backSample = (long) (fftDataUnit.getStartSample() - whistleMoanControl.getWhistleToneParameters().getBackgroundInterval() * getSampleRate());
			saveBackgroundMeasurements(lastBackgroundTime, backSample);
		}
	}

	/**
	 * Get background measurements and add them to the output data. 
	 * @param backgroundTime Time at end of measurement
	 */
	private void saveBackgroundMeasurements(long backgroundTime, long backgroundSample) {
		if (spectrumBackgrounds == null) {
			return;
		}
		double scale = 2./spectrumBackgrounds.getFftDataBlock().getFftLength();
		double dur = (long) (whistleMoanControl.getWhistleToneParameters().getBackgroundInterval() * 1000.);
		for (int i = 0; i < shapeConnectors.length; i++) {
			ShapeConnector sc = shapeConnectors[i];
			if (sc == null) continue;
			double[] bgData = spectrumBackgrounds.copyBackground(sc.firstChannel);
			if (bgData == null) {
				continue;
			}
			for (int f = 0; f < bgData.length; f++) {
				bgData[f] = Math.sqrt(bgData[f] * scale);
			}
			SpecBackgroundDataUnit sbdu = new SpecBackgroundDataUnit(backgroundTime, backgroundSample, 1<<sc.firstChannel, dur, 0, bgData.length, bgData);
			specBackgroundManager.addData(sbdu);
		}
		
	}


	@Override
	public void pamStart() {
		localisationCPU.reset();
	}

	@Override
	public void pamStop() {
		if (SMRUEnable.isEnable()) {
			//			System.out.println(localisationCPU.getSummary("Whistle TOAD Localisation: "));
		}
	}

	@Override
	public void setupProcess() {
		super.setupProcess();
		SpectrogramNoiseProcess snp = whistleMoanControl.getSpectrogramNoiseProcess();
		setParentDataBlock(snp.getOutputDataBlock());
		if (whistleMoanControl.whistleToneParameters.getDataSource() == null) {
			return;
		}
		//		sourceData = (FFTDataBlock) PamController.getInstance().getDataBlock(FFTDataUnit.class, 
		//				whistleMoanControl.whistleToneParameters.getDataSource());
		//		snp.setParentDataBlock(sourceData);
		sourceData = (FFTDataBlock) getParentDataBlock(); // our source should always be the output of the SpectrogramNoiseProcess
		SpectrogramNoiseSettings specnoiseSettings = whistleMoanControl.whistleToneParameters.getSpecNoiseSettings();
		specnoiseSettings.dataSource = whistleMoanControl.whistleToneParameters.getDataSource();
		snp.setNoiseSettings(specnoiseSettings);

		chanOrSeqMap = whistleMoanControl.whistleToneParameters.getChanOrSeqBitmap();	// the channelMap in WhistleToneParameters object may be a sequence map or a channel map, depending on source
		//		if (sourceData != null) {
		////			chanOrSeqMap = getParentDataBlock().getChannelMap() & 
		//			chanOrSeqMap = getParentDataBlock().getSequenceMap() & // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output 
		//					whistleMoanControl.whistleToneParameters.getChanOrSeqBitmap();
		//			outputData.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), chanOrSeqMap);
		//			outputData.setFftHop(sourceData.getFftHop());
		//			outputData.setFftLength(sourceData.getFftLength());
		//			
		//			// 2017/11/30 set the whistleLocations channelMap properly
		//			whistleLocations.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), chanOrSeqMap);
		//			
		//			//			smoothingChannelProcessList = new SmoothingChannelProcess[PamUtils.getHighestChannel(chanOrSeqMap)+1];
		//			//			for (int i = 0; i < PamUtils.getHighestChannel(chanOrSeqMap)+1; i++) {
		//			//				smoothingChannelProcessList[i] = new SmoothingChannelProcess();
		//			//			}
		//		}
		// set the localisation information in the two output datablocks. If the source is using sequence numbers, then we cannot localise
		boolean mayBearings = whistleMoanControl.whistleToneParameters.mayHaveBearings();
		boolean mayRange = whistleMoanControl.whistleToneParameters.mayHaveRange();
		if (getParentDataBlock().getSequenceMapObject()!=null) {
			mayBearings=false;
			mayRange=false;
		}
		if (mayBearings) {
			outputData.setLocalisationContents(LocContents.HAS_BEARING);
			whistleLocations.setLocalisationContents(LocContents.HAS_BEARING);
		}
		else {
			outputData.setLocalisationContents(0);
			whistleLocations.setLocalisationContents(0);
		}
		if (mayRange) {
			whistleLocations.setLocalisationContents(LocContents.HAS_BEARING |
					LocContents.HAS_RANGE);
		}
		if (mayBearings || mayRange) {
			outputData.SetLogging(whistleToneLogging = new WhistleToneLogging(whistleMoanControl, 
					outputData, SQLLogging.UPDATE_POLICY_WRITENEW));
			whistleToneLogging.reCheckTable();
		}

		prepareProcess();
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		
		/*
		 * Need to work out which is the right process to get background information from. 
		 */

		// reset these, just in case the source has changed
		chanOrSeqMap = sourceData.getSequenceMap() & // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output 
				whistleMoanControl.whistleToneParameters.getChanOrSeqBitmap();
		outputData.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), chanOrSeqMap);
		outputData.setFftHop(sourceData.getFftHop());
		outputData.setFftLength(sourceData.getFftLength());
		whistleLocations.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), chanOrSeqMap);

		nConnectors = whistleMoanControl.whistleToneParameters.countChannelGroups();
		shapeConnectors = new ShapeConnector[nConnectors];
		int groupChannels;
		int firstChannels = 0;
		for (int i = 0; i < nConnectors; i++) {
			groupChannels = whistleMoanControl.whistleToneParameters.getGroupChannels(i);
			shapeConnectors[i] = new ShapeConnector(i, groupChannels, 
					whistleMoanControl.whistleToneParameters.getConnectType());
			firstChannels |= 1<<shapeConnectors[i].firstChannel;
			shapeConnectors[i].initialise();
		}
		detectionGrouper.setupGroups(whistleMoanControl.whistleToneParameters);

		clearSummaryData();
		
		FFTDataBlock rawFFTBlock = findNoisyFFTData(sourceData);
		if (rawFFTBlock == null) {
			spectrumBackgrounds = null;
		}
		else if (spectrumBackgrounds == null || spectrumBackgrounds.getFftDataBlock() != rawFFTBlock) {
			spectrumBackgrounds = new SpectrumBackgrounds(rawFFTBlock, firstChannels);
		}
		if (spectrumBackgrounds != null) {
			spectrumBackgrounds.prepareS(firstChannels, 10);
		}
		if (spectrumBackgroundObserver != null && spectrumBackgroundObserver.rawFFTBlock != rawFFTBlock) {
			spectrumBackgroundObserver.remove();
			spectrumBackgroundObserver = null;
		}
		if (spectrumBackgroundObserver == null && rawFFTBlock != null) {
			spectrumBackgroundObserver = new SpectrumBackgroundObserver(rawFFTBlock);
		}
		lastBackgroundTime = PamCalendar.getTimeInMillis();
		

//		System.out.println("Original FFT data is " + rawFFTBlock.getLongDataName());
	}
	
	/**
	 * Observer for spectrogram background measurements, which come from the raw fft data before 
	 * any noise processing. 
	 * @author dg50
	 *
	 */
	private class SpectrumBackgroundObserver extends PamObserverAdapter {

		private FFTDataBlock rawFFTBlock;
		
		public SpectrumBackgroundObserver(FFTDataBlock rawFFTBlock) {
			super();
			this.rawFFTBlock = rawFFTBlock;
			rawFFTBlock.addObserver(this);
		}

		public void remove() {
			if (rawFFTBlock != null) {
				rawFFTBlock.deleteObserver(this);
			}
		}

		@Override
		public String getObserverName() {
			return getProcessName() + " Background";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			if (spectrumBackgrounds != null && spectrumBackgrounds.getFftDataBlock() == observable) {
//				if (pamDataUnit.getChannelBitmap() == 1) {
//					System.out.println("Updatebackground data to " + spectrumBackgrounds.copyBackground(0)[10]);
//				}
				spectrumBackgrounds.process((FFTDataUnit) pamDataUnit); 
			}
		}
		
	}
	
	/**
	 * find the original FFT or possibly beam former data prior to any noise reduction
	 * @return an upstream FFT Block which isn't from a noise process. 
	 */
	private FFTDataBlock findNoisyFFTData(FFTDataBlock sourceBlock) {
		if (sourceBlock == null) {
			return null;
		}
		PamProcess sbProcess = sourceBlock.getParentProcess();
		if (sbProcess == null) {
			return sourceBlock;
		}
		if (sbProcess instanceof SpectrogramNoiseProcess == false) {
			return sourceBlock;
		}
		else {
			PamDataBlock procParent = sbProcess.getParentDataBlock();
			if (procParent instanceof FFTDataBlock) {
				return findNoisyFFTData((FFTDataBlock) procParent);
			}
		}
		return sourceBlock;
	}

	protected int getFFTLen() {
		if (sourceData != null) {
			return sourceData.getFftLength();
		}
		return 2;
	}

	/**
	 * Find the appropriate shape connector for the given channels. 
	 * The first time this gets called in viewer mode, there
	 * will be nothing there,so call setupProcess to create them.  
	 * @param channelMap channel bitmap we're looking for. 
	 * @return a ShapeConnector or null if the channelMap doesn't 
	 * match any existing connectors for this configuration. 
	 */
	public ShapeConnector findShapeConnector(int channelMap) {
		if (shapeConnectors == null || shapeConnectors[0] == null) {
			setupProcess();
		}
		for (int i = 0; i < nConnectors; i++) {
			if ((shapeConnectors[i].groupChannels & channelMap) != 0) {
				return shapeConnectors[i];
			}
		}
		return null;
	}

	public class ShapeConnector {

		private ConnectedRegion[][] regionArray = new ConnectedRegion[2][];
		private boolean[] spacedArray;

		final private int[] search8x = {1, 0, 0, 0};
		final private int[] search8y = {-1, -1, 0, 1};
		final private int[] search4x = {1, 0};
		final private int[] search4y = {-1, 0};
		private int[] searchx; 
		private int[] searchy;

		/**
		 * channelMap or sequenceMap for this ShapeConnectors' group (depending on source)
		 */
		private int groupChannels;

		/**
		 * the first channel/sequence number in this group
		 */
		private int firstChannel;

		private int iD;
		private boolean[] newCol;

		int searchBin1, searchBin2;

		int regionNumber = 0;

		private WhistleDelays whistleDelays;

		LinkedList<ConnectedRegion> growingRegions;
		LinkedList<ConnectedRegion> recycleRegions;

		private RegionFragmenter regionFragmenter = new NullFragmenter(); 

		private BearingLocaliser bearingLocaliser;
		private int hydrophoneMap;
		private double maxDelaySeconds;

		/**
		 * ShapeConnector object constructor.  Note that the groupChannels bitmap may actually refer to sequence numbers and not channels, if
		 * the source is beamformer output
		 * 
		 * @param iD
		 * @param groupChannels channelMap or sequenceMap, depending on source
		 * @param connectType
		 */
		ShapeConnector(int iD, int groupChannels, int connectType) {
			this.iD = iD;
			this.groupChannels = groupChannels;
			// this next line gets updated to the correct map before any localisation takes place !
			hydrophoneMap = groupChannels;

			this.firstChannel = PamUtils.getLowestChannel(groupChannels);
			setConnectionType(connectType);
			growingRegions = new LinkedList<ConnectedRegion>();
			recycleRegions = new LinkedList<ConnectedRegion>();

			// do a quick check to see if the source has sequence numbers.  If it does, then we can't localise.  The easiest
			// way to prevent localisation is to send channelMap=0 to the whistleDelays object
			if (sourceData.getSequenceMapObject()!=null) {
				whistleDelays = new WhistleDelays(whistleMoanControl, 0);
			} else {
				whistleDelays = new WhistleDelays(whistleMoanControl, groupChannels);
			}
			switch(whistleMoanControl.whistleToneParameters.fragmentationMethod) {
			case WhistleToneParameters.FRAGMENT_NONE:
				regionFragmenter = new NullFragmenter();
				break;
			case WhistleToneParameters.FRAGMENT_DISCARD:
				regionFragmenter = new DiscardingFragmenter();
				break;
			case WhistleToneParameters.FRAGMENT_FRAGMENT:
				regionFragmenter = new FragmentingFragmenter(whistleMoanControl);
				break;
			case WhistleToneParameters.FRAGMENT_RELINK:
				regionFragmenter = new RejoiningFragmenter(whistleMoanControl);
				break;
			}
		}

		/**
		 * 
		 */
		private void initialise() {
			regionNumber = 0;
			if (sourceData == null) {
				return;
			}
			// only create bearing localiser objects if we have a channel list manager and we're not dealing with sequence numbers
			if (sourceData.getChannelListManager() != null && sourceData.getSequenceMapObject()==null) {
				hydrophoneMap = sourceData.getChannelListManager().channelIndexesToPhones(groupChannels);
				double timingError = Correlations.defaultTimingError(getSampleRate());
				bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(hydrophoneMap, timingError); 
				maxDelaySeconds = ArrayManager.getArrayManager().getCurrentArray().getMaxPhoneSeparation(hydrophoneMap, PamCalendar.getTimeInMillis()) / 
						ArrayManager.getArrayManager().getCurrentArray().getSpeedOfSound();
			}
			whistleDelays.prepareBearings();
			searchBin1 = (int) (whistleMoanControl.whistleToneParameters.getMinFrequency() * 
					sourceData.getFftLength() / getSampleRate());			
			searchBin2 = (int) (whistleMoanControl.whistleToneParameters.getMaxFrequency(getSampleRate()) * 
					sourceData.getFftLength() / getSampleRate());
			searchBin1 = Math.max(0, Math.min(sourceData.getFftLength()/2, searchBin1));
			searchBin2 = Math.max(0, Math.min(sourceData.getFftLength()/2, searchBin2));
		}

		/**
		 * Gets passed a row of Complex data. 
		 * Converts to boolean array and passes to 
		 * boolean function with same name. 
		 * @param complexData array of Complex data. 
		 */
		public void newData(int iSlice, FFTDataUnit fftDataUnit) {
			ComplexArray complexData = fftDataUnit.getFftData();
			if (newCol == null || newCol.length != complexData.length()) {
				newCol = new boolean[complexData.length()];
			}
			for (int i = searchBin1; i < searchBin2; i++) {
				newCol[i] = complexData.magsq(i) > 0;
			}
			newData(iSlice, newCol, fftDataUnit);
		}


		/**
		 * Gets passed a row of boolean values representing
		 * thresholded fft data.  
		 * @param newData boolean array. 
		 */
		private void newData(int iSlice, boolean[] newData, FFTDataUnit fftDataUnit) {
			// need space either side for region search. 
			int dataLen = newData.length;
			int regLen = dataLen + 2;
			int space = 1;
			ConnectedRegion connectedRegion, thisRegion;
//			if (fftDataUnit.getTimeMilliseconds() >= 1226371634117L  && fftDataUnit.getTimeMilliseconds() < 1226371634218L) {
//				int pCount = 0;
//				for (int i = 200; i < 350; i++) {
//					if (newData[i]) {
//						pCount++;
//						System.out.printf("*");
//					}
//					else {
//						System.out.printf("_");
//					}
//				}
//				debugPause = true;
////				if (pCount > 0) {
//					System.out.printf("CR %d bins at %s\n", pCount,  PamCalendar.formatDBDateTime(fftDataUnit.getTimeMilliseconds(), true));
////				}
//			}

			if (regionArray == null || regionArray[0] == null || regionArray[1] == null ||
					regionArray[1].length != regLen || regionArray[0].length != regLen) {
				regionArray = new ConnectedRegion[2][regLen];
			}
			if (spacedArray == null || spacedArray.length != regLen) {
				spacedArray = new boolean[regLen];
			}
			// now copy the data into the spaced array. 
			for (int i = 0; i < dataLen; i++) {
				spacedArray[i+space] = newData[i];
			}

			// flag all regions as NOT growing so that finished
			// ones can be sent on their way.
			labelGrowing(false);

			// loop over new data
			for (int i = space; i < dataLen+space; i++) {
				if (spacedArray[i] == false) {
					continue; // continue of this pixel is not set. 
				}
				/*
				 * Loop over search pixels. There are three options
				 * here.
				 * 1) it connects to nothing, so it's a new region
				 * 2) it connects to something immediately, but isn't assigned 
				 * to anything itself yet. 
				 * 3) it's already assigned itself due to either 1 or 2 above
				 * but then needs to re-assign
				 */
				thisRegion = null;
				for (int si = 0; si < searchx.length; si++) {
					connectedRegion = regionArray[searchx[si]][i+searchy[si]];
					if (connectedRegion == null) {
						// new region. Don't actually do anything yet !
					}
					else if (thisRegion == null) {
						/* 
						 * This cell is not yet assigned to any region, 
						 * so add it to the one we've just found. 
						 */
						thisRegion = connectedRegion;
						if (connectedRegion.getSliceData().size() > 2000) {
							System.out.println("*** Very large region in " + whistleMoanControl.getUnitName() + " at " + PamCalendar.formatDateTime(connectedRegion.getStartMillis()));
						}
						else {
							connectedRegion.addPixel(iSlice, i-space, fftDataUnit);
						}
					}
					else if (thisRegion != connectedRegion){
						/*
						 * The cell is already assigned to a region, but 
						 * that region is not the same as this region, so 
						 * two regions need to be merged. 
						 * Take the first and merge the second onto it.  
						 */
						thisRegion = mergeRegions(thisRegion, connectedRegion);
						//						thisRegion.mergeRegion(connectedRegion);
					}
					regionArray[1][i] = thisRegion;
					/*
					 * Now pop back down the column, to include anything that is touching
					 * this region, but didn't get assigned. 
					 */
					for (int ii = i-1; ii >0; ii--) {
						if (spacedArray[ii] == false || regionArray[1][ii] != null) {
							break;
						}
						regionArray[1][ii] = regionArray[1][i];
					}
				} // end of search loop around this pixel
			} // end of loop up slice
			/**
			 * Since isolate pixels which did not connect to a region in the preceeding slice
			 * were ignored in the previous iteration (to avoid too much object creation) now
			 * need to run up the column again creating new regions. 
			 */
			for (int i = space; i < dataLen+space; i++) {
				if (spacedArray[i] == false || regionArray[1][i] != null) {
					continue; // continue of this pixel is not set. 
				}
				/*
				 * See if it links in with anything underneath it.
				 */
				if (regionArray[1][i-1] != null) {
					regionArray[1][i] = regionArray[1][i-1];
				}
				else {
					/*
					 *  it's only here that we actually create new regions.
					 *  i.e. at the bottom of a group of regions which have already 
					 *  been shown not to connect to any others.  
					 */
					regionArray[1][i] = createNewRegion(iSlice, i-space, dataLen, fftDataUnit);
				}
			}

			/*
			 * Put debug point in at time 1226371634202 which is when the big regions should end
			 */
//			if (fftDataUnit.getTimeMilliseconds() >= 1226371634202L) {
//				System.out.println("End big one");
//			}
			findCompleteRegions();

			// now shuffle the region arrays along and create a blank one ready for next 
			// call
			regionArray[0] = regionArray[1];
			regionArray[1] = new ConnectedRegion[regLen];
		}

		/**
		 * Merge two regions together. Merge the one that started second onto the one
		 * that started first to ensure that all slices are present in the master region
		 * @param r1 region 1
		 * @param r2 region 2
		 * @return reference to the remaining region. The other get's binned or recycled. 
		 */
		private ConnectedRegion mergeRegions(ConnectedRegion r1, ConnectedRegion r2) {
			ConnectedRegion m, s;
			if (r1.getFirstSlice() < r2.getFirstSlice()) {
				m = r1;
				s = r2;
			}
			else {
				m = r2;
				s = r1;
			}

			m.mergeRegion(s);

			removeRegion(s);

			int nCol = regionArray.length;
			int nRow = regionArray[0].length;
			for (int iCol = 0; iCol < nCol; iCol++) {
				for (int iRow = 0; iRow < nRow; iRow++) {
					if (regionArray[iCol][iRow] == s) {
						regionArray[iCol][iRow] = m;
					}
				}
			}
			return m;
		}
		/**
		 * Will eventually set a recycling scheme, but not now. 
		 * @param iSlice
		 * @param iCell
		 * @return new or recycled region
		 */
		private ConnectedRegion createNewRegion(int iSlice, int iCell, int dataLen, FFTDataUnit fftDataUnit) {
			ConnectedRegion newRegion;
			if (recycleRegions.size() > 0) {
				newRegion = recycleRegions.removeLast();
				newRegion.resetRegion(firstChannel, iSlice, regionNumber++, dataLen);
			}
			else {
				newRegion = new ConnectedRegion(firstChannel, iSlice, regionNumber++, dataLen);
			}
			newRegion.addPixel(iSlice, iCell, fftDataUnit);
			growingRegions.add(newRegion);
			return newRegion;
		}

		private void removeRegion(ConnectedRegion r) {
			growingRegions.remove(r);
			recycleRegion(r);
		}

		/**
		 * Remove a region from the growing list and recyce it. 
		 * @param r region to remove. 
		 */
		private void recycleRegion(ConnectedRegion r) {
			if (recycleRegions.size() < 20) {
				r.recycle();
				recycleRegions.add(r);
			}
		}


		private void labelGrowing(boolean growing) {
			ListIterator<ConnectedRegion> rl = growingRegions.listIterator();
			while(rl.hasNext()) {
				rl.next().setGrowing(growing);
			}
		}

		private void findCompleteRegions() {
			ListIterator<ConnectedRegion> rl = growingRegions.listIterator();
			ConnectedRegion r;
			while(rl.hasNext()) {
				r=rl.next();
				if (r.isGrowing() == false) {
					rl.remove();
					if (completeRegion(r) == false) {
						recycleRegion(r);
					}
				}
			}
		}

		private boolean completeRegion(ConnectedRegion region) {
			if (!wantRegion(region)) {
				return false;
			}
			
			//				region.sayRegion();
			region.condenseInfo();
			
//			if (region.getFirstSlice() == 44647) {
//				System.out.println("Defrag big one");
			if (whistleMoanControl.getWhistleToneParameters().keepShapeStubs == false) {
				stubRemover.removeStubs(region);
			}
//			}

			regionFragmenter.fragmentRegion(region);
			
			int nFrag = regionFragmenter.getNumFragments();
			//				if (nFrag == 5) {
			//					region.sayRegion();
			//					region.sayRegion();
			//					nFrag = regionFragmenter.fragmentRegion(region);
			//				}
			for (int i = 0; i < nFrag; i++) {
				region = regionFragmenter.getFragment(i);
				completeRegionFragment(region);
			}
			return nFrag > 0;
		}
		


		private boolean wantRegion(ConnectedRegion region) {
			// first two checks on size of whistle. 
			if (region.getTotalPixels() < whistleMoanControl.whistleToneParameters.minPixels) {
				return false;
			}
			if (region.getNumSlices() < whistleMoanControl.whistleToneParameters.minLength) {
				return false;
			}
			// then bin any whistles in first 1/2 second since they are often noisy. 
			if (region.getStartSample() < getSampleRate()/4) {
				return false;
			}

			return true;
		}

		private void completeRegionFragment(ConnectedRegion region) {
			ConnectedRegionDataUnit newData = new ConnectedRegionDataUnit(region, THAT);
			// set the full hydrophone map - changes the way data are displayed slightly !
			//			newData.setChannelBitmap(groupChannels);
			newData.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), groupChannels);

			localisationCPU.start();
			double[] delays = whistleDelays.getDelays(region);
			double[][] anglesAndErrors = null;
			if (delays != null) {
				for (int i = 0; i < delays.length; i++) {
					delays[i] /= getSampleRate();
				}
//				if (delays.length > 0) {
//					Debug.out.printf("Whistle delay = %3.3f ms\n", delays[0]);
//				}
				if (bearingLocaliser != null) {
					anglesAndErrors = bearingLocaliser.localise(delays,newData.getTimeMilliseconds());
				}
				//				if (SMRUEnable.isEnable()) {
				//					BearingLocAnnotation bla = new BearingLocAnnotation(bearingLocAnnotationType, anglesAndErrors);
				//					newData.addDataAnnotation(bla);
				//				}
				WhistleBearingInfo newLoc = new WhistleBearingInfo(newData, bearingLocaliser, 
						hydrophoneMap, anglesAndErrors);
				if (bearingLocaliser != null) {
					newLoc.setArrayAxis(bearingLocaliser.getArrayAxis());
				}
				//				System.out.println("Timing delay = " + delays[0] + " channels " + groupChannels);
				newData.setTimeDelaysSeconds(delays);
				newData.setLocalisation(newLoc);
				//				newData.getBasicData().seta
			}
			localisationCPU.stop();
			// work out the amplitude
			double a = region.calculateRMSAmplitude();
			//			System.out.println("RMS amplitude = " + a);
			//				region.sayRegion();
			newData.setMeasuredAmpAndType(a, DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD);
			ArrayList<ConnectedRegionDataUnit> matchedUnits = detectionGrouper.findGroups(newData);
			if (matchedUnits != null && matchedUnits.size() == 1 && newData.getSequenceBitmapObject()==null) {
				// have one matched whistle, so try to get a location from it
				//					System.out.println("Matched unit found");
				WhistleToneGroupedDetection wgd = new WhistleToneGroupedDetection(matchedUnits.get(0));
				wgd.addSubDetection(newData);
				boolean ok1 = detectionGroupLocaliser.localiseDetectionGroup(wgd, 1);
				boolean ok2 = detectionGroupLocaliser.localiseDetectionGroup(wgd, -1);
				if (ok1 || ok2) {
					whistleLocations.addPamData(wgd);
					//						newData.setLocalisation(wgd.getLocalisation());
				}
			}
			summariseWhistle(newData);
			outputData.addPamData(newData);
		}
		public void setConnectionType(int searchType) {
			if (searchType == 4) {
				searchx = search4x;
				searchy = search4y;
			}
			else {
				searchx = search8x;
				searchy = search8y;
			}
		}

		/**
		 * @return the bearingLocaliser
		 */
		public BearingLocaliser getBearingLocaliser() {
			return bearingLocaliser;
		}

		/**
		 * @return the groupChannels
		 */
		public int getGroupChannels() {
			return groupChannels;
		}

		private synchronized void summariseWhistle(ConnectedRegionDataUnit newData) {
			ConnectedRegion cr = newData.getConnectedRegion();
			int nS = cr.getNumSlices();
			int nP;
			List<SliceData> sliceData = cr.getSliceData();
			//		SliceData aSlice;
			/**
			 * Get measures of signal and noise in the standard form re 1 count
			 */
			double[] bgData = spectrumBackgrounds.copyBackground(this.firstChannel);
			double ampScale = 2./(double) spectrumBackgrounds.getFftDataBlock().getFftLength();
			int topBin = 0;
			int botBin = bgData.length-1;
			double sig = 0;
			
			int sumBin;
			for (SliceData aSlice:sliceData) {
				nP = aSlice.nPeaks;
				ComplexArray fftArray = aSlice.fftDataUnit.getFftData();
				for (int i = 0; i < nP; i++) {
					int[] pInf = aSlice.peakInfo[i];
					sumBin = pInf[1] / summaryBlockSize;
					whistleSummaryCount[sumBin]++;
					topBin = Math.max(topBin, pInf[2]);
					botBin = Math.min(botBin, pInf[0]);
					for (int f = pInf[0]; f <= pInf[1]; f++) {
						sig += fftArray.magsq(f);
					}
				}
			}	
			double nse = 0;
			for (int i = botBin; i <= topBin; i++) {
				nse += bgData[i];
			}
			nse = Math.sqrt(nse*ampScale);
			newData.setNoiseBackground((float) nse); 
			sig = Math.sqrt(sig*ampScale/sliceData.size());
			newData.setSignalSPL((float) sig);
		}
	}

	public ConnectedRegionDataBlock getOutputData() {
		return outputData;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getFrequencyRange()
	 */
	@Override
	public double[] getFrequencyRange() {
		double[] fRange = super.getFrequencyRange();
		fRange[0] = whistleMoanControl.getWhistleToneParameters().getMinFrequency();
		fRange[1] = whistleMoanControl.getWhistleToneParameters().getMaxFrequency(getSampleRate());
		return fRange;
	}


	public double[] getDurationRange() {
		double fs = getSampleRate();
		WhistleToneParameters params = whistleMoanControl.whistleToneParameters;
		if (sourceData == null) {
			return null;
		}
		double fftHop = sourceData.getFftHop(); 
		double[] dRange = {params.minLength * fftHop / fs, Double.POSITIVE_INFINITY};
		return dRange;
	}


	private void clearSummaryData() {
		if (sourceData == null) {
			return;
		}
		int fftLen = sourceData.getFftLength();
		summaryBlockSize = fftLen / 2 / NSUMMARYPOINTS;
		for (int i = 0; i < NSUMMARYPOINTS; i++) {
			whistleSummaryCount[i] = 0;
		}
	}


	/**
	 * When delay data are written to binary files, int16's are used, but these
	 * must be scaled up to allow for sub-sample timing. How much they can be 
	 * scaled depends a lot on the array spacing, sample rate and FFT length. 
	 * @return scale factor which will give the highest timing resolution without overflows. 
	 */
	public int getDelayScale() {
		// work out the max delay and then work out a scaling factor. 
		double maxSep = 0;
		double sep;
		for (int i = 0; i < nConnectors; i++) {
			sep = shapeConnectors[i].maxDelaySeconds;
			maxSep = Math.max(maxSep, sep);
		}
		maxSep *= getSampleRate();
		if (maxSep == 0) return 1;
		maxSep *= 1.1;
		int delayScale = 1;
		while (maxSep * delayScale < 16384) {
			delayScale *= 2;
		}
		return delayScale;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#processNewBuoyData(networkTransfer.receive.BuoyStatusDataUnit, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void processNewBuoyData(BuoyStatusDataUnit statusDataUnit,
			PamDataUnit dataUnit) {
		ConnectedRegionDataUnit crdu = (ConnectedRegionDataUnit) dataUnit;
		ShapeConnector shapeConnector = findShapeConnector(chanOrSeqMap);
		if (shapeConnector != null) {
			BearingLocaliser bl = shapeConnector.getBearingLocaliser();
			if (bl != null) {
				double[] delays = crdu.getTimeDelaysSeconds();
				if (delays != null && delays.length > 0) {
//					Debug.out.printf("Whistle time delay = %3.2f millisecs\n" ,  crdu.getTimeDelaysSeconds()[0]*1000.);
				}
				double[][] angles = bl.localise(crdu.getTimeDelaysSeconds(), crdu.getTimeMilliseconds());
				if (angles != null) {
					WhistleBearingInfo newLoc = new WhistleBearingInfo(crdu, bl, 
							shapeConnector.getGroupChannels(), angles);
					newLoc.setArrayAxis(bl.getArrayAxis());
					//					newLoc.set
					newLoc.setSubArrayType(bl.getArrayType());
					crdu.setLocalisation(newLoc);
				}
			}
		}
	}

	/**
	 * Get the list of FFT data that input into a connected region 
	 * the given channel list. 
	 * @param connectedRegion connected region. 
	 * @param channelBitmap required channels
	 * @return channel interleaved list of FFT data.
	 */
	public List<FFTDataUnit> getFFTInputList(ConnectedRegion connectedRegion, int channelBitmap) {
		if (sourceData == null) {
			return null;
		}
		int nChan = PamUtils.getNumChannels(channelBitmap);
		int nSlice = connectedRegion.getNumSlices();
		ArrayList<FFTDataUnit> fftUnitsOut = new ArrayList<>(nChan*nSlice);
		int iSlice = 0;
		List<SliceData> sliceDataList = connectedRegion.getSliceData();
		FFTDataUnit fftUnit;
		int firstChan = PamUtils.getLowestChannel(channelBitmap);
				
		synchronized (sourceData) {
			ListIterator<FFTDataUnit> fftIterator = 
					sourceData.getListIteratorFromEnd(connectedRegion.getStartMillis(), 1<<firstChan, 
							PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
			if (fftIterator == null) {
				return null;
			}
			for (SliceData sliceData:sliceDataList) {
				int fftLen = sliceData.sliceLength;
				long fftStartSample = sliceData.startSample;
				if (sliceData.getFftDataUnit() != null) {
					fftStartSample = sliceData.getFftDataUnit().getStartSample();
				}
				int foundCh = 0;
				int[] usefulBinRange = sliceData.getUsefulBinRange();
				while (fftIterator.hasNext()) {
					fftUnit = fftIterator.next();
					// see if we already have this channel for this slice
					if ((foundCh & fftUnit.getChannelBitmap()) != 0) {
						// try to go back one !
						if (fftIterator.hasPrevious()) {
							fftIterator.previous();
						}
						break;
					}
					if (fftUnit.getStartSample()<fftStartSample) {
						continue;
					}
					if (fftUnit.getStartSample() > fftStartSample+sliceData.sliceLength) {
						break;
					}
					if ((fftUnit.getChannelBitmap() & channelBitmap) == 0) {
						continue;
					}
					fftUnit.setUsefulBinRange(usefulBinRange);
					fftUnitsOut.add(fftUnit);
//					System.out.printf("Add FFT start %dms channel %d\n",
//							fftUnit.getTimeMilliseconds()%10000, PamUtils.getSingleChannel(fftUnit.getChannelBitmap()));
					foundCh |= fftUnit.getChannelBitmap();
					if (foundCh == channelBitmap) {
						break;
					}
				}
			}
		}
		return fftUnitsOut;
	}

	/*
	 * replicate Decimus function to give counts of whistles in four evenly spaced frequency 
	 * bins. 
	 */
	public String getModuleSummary(boolean clear) {
		String sumText = String.format("%d", NSUMMARYPOINTS);
		for (int i = 0; i < NSUMMARYPOINTS; i++) {
			sumText += String.format(",%d",whistleSummaryCount[i]);
		}
	
		if (clear) {
			clearSummaryData();
		}
		return sumText;
	}

}
