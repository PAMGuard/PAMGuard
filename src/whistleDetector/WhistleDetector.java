/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package whistleDetector;

import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import PamController.PamController;
import PamDetection.LocContents;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.dialog.GroupedSourcePanel;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.SQLLogging;
import whistlesAndMoans.AbstractWhistleDataBlock;

/**
 * @author Doug Gillespie
 *         <p>
 *         Re-implementation of the IFAW 2003 whistle detector
 *         <p>
 *         Uses the PeakDetector and WhistleLinker to do the actual work
 */
public class WhistleDetector extends PamProcess {


	WhistleControl whistleControl;
	
	PeakDetector[] peakDetectors;

	WhistleLinker[] whistleLinkers;
	
	WhistleEventDetector eventDetector;

	PamDataBlock<PeakDataUnit> peakDataBlock;
	
	AbstractWhistleDataBlock whistleDataBlock;
	
	FFTDataBlock supressedSpectrogram;
		
	/**
	 * Cross channel grouping. 
	 */
	PamDataBlock<WhistleGroupDetection> whistleLocations;
	
//	AcousticDataBlock<FFTDataUnit> fftDataBlock;
	
	AcquisitionProcess daqProcess;
	
	FFTDataBlock fftDataBlock;

	/*
	 * Control information lifted from parent processes
	 */
	int fftLength;

	int fftHop;

//	int channel;

//	double maxDF, maxD2F;

	long currentSample;
	
	private int nChannelGroups;

	DetectionGroupLocaliser detectionGroupLocaliser;

	/**
	 * @param whistleControl reference ot a WhistleControl PamControlledUnit pamguard plug-in
	 */
	public WhistleDetector(WhistleControl whistleControl) {

		super(whistleControl, null); 
//		super(whistleControl, parentFFTProcess.GetOutputDataBlock(0));// ,
																
		this.whistleControl = whistleControl;


//		peakDetector = new BetterPeakDetector(whistleControl, this,
//				(PamFFTProcess) getParentProcess());
//
//		whistleLinker = new WhistleLinker(whistleControl, this,
//				peakDetector);
		

		// and this subscribes to the Linker (I think)
		// whistleLinker.GetOutputDataBlock(0).addObserver(this);

		// this whistle detector needs to subscribe too
		//inputData.addObserver(this);

		whistleDataBlock = new WhistleDataBlock(ShapeDataUnit.class, 
				"Whistles",	this, whistleControl.whistleParameters.channelBitmap);
		addOutputDataBlock(whistleDataBlock);
		
		whistleDataBlock.setOverlayDraw(new WhistleGraphics(this));
		StandardSymbolManager symbolManager = new StandardSymbolManager(whistleDataBlock, WhistleGraphics.defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		whistleDataBlock.setPamSymbolManager(symbolManager);

		whistleDataBlock.setLocalisationContents(LocContents.HAS_AMBIGUITY | LocContents.HAS_BEARING);
		whistleDataBlock.SetLogging(new WhistleLogger(whistleControl, whistleDataBlock));
		
		whistleLocations = new PamDataBlock<WhistleGroupDetection>(WhistleGroupDetection.class,
				"Localised Whistles", this, whistleControl.whistleParameters.channelBitmap);
		whistleLocations.setOverlayDraw(new WhistleLocalisationGraphics(whistleLocations));
		whistleLocations.setPamSymbolManager(new StandardSymbolManager(whistleLocations, WhistleLocalisationGraphics.defaultSymbol, true));
		addOutputDataBlock(whistleLocations);
		whistleLocations.setLocalisationContents(LocContents.HAS_BEARING | LocContents.HAS_RANGE |
				LocContents.HAS_LATLONG | LocContents.HAS_PERPENDICULARERRORS| LocContents.HAS_AMBIGUITY);
		whistleLocations.SetLogging(new PamDetectionLogging(whistleLocations, SQLLogging.UPDATE_POLICY_WRITENEW));
		
		supressedSpectrogram = new FFTDataBlock("Noise supressed spectrogram", this, 3, 0, 0);
		addOutputDataBlock(supressedSpectrogram);
		
		detectionGroupLocaliser = new DetectionGroupLocaliser(this);

	}
	
	private int peakDetectorType = -1;
	private void buildDetectors() {
		// build a peak detector and linker for each channel group.
		nChannelGroups = GroupedSourcePanel.countChannelGroups(whistleControl.whistleParameters.channelBitmap, 
				whistleControl.whistleParameters.channelGroups);
		
		if (nChannelGroups <= 0) {
			destroyDetectors();
			return;
		}
		if (fftDataBlock == null) {
			return;
		}
		if (peakDetectors == null || peakDetectors.length != nChannelGroups || 
				peakDetectorType != whistleControl.whistleParameters.peakDetectionMethod) {
			destroyDetectors();
			peakDetectorType = whistleControl.whistleParameters.peakDetectionMethod;
			peakDetectors = new PeakDetector[nChannelGroups];
			whistleLinkers = new WhistleLinker[nChannelGroups];
			PeakDetectorProvider pdProvider = whistleControl.peakDetectorProviders.get(peakDetectorType);
			int groupChannels;
			for (int i = 0; i < nChannelGroups; i++) {
				groupChannels = GroupedSourcePanel.getGroupChannels(i, whistleControl.whistleParameters.channelBitmap, 
						whistleControl.whistleParameters.channelGroups);
				peakDetectors[i] = pdProvider.createDetector(whistleControl, this, fftDataBlock, groupChannels);
//				peakDetectors[i] = new BetterPeakDetector(whistleControl, this, (PamFFTProcess) getParentProcess(), groupChannels);
				whistleLinkers[i] = new WhistleLinker(whistleControl, this, peakDetectors[i], groupChannels);
			}
		}
		else {
			int groupChannels;
			for (int i = 0; i < nChannelGroups; i++) {
				groupChannels = GroupedSourcePanel.getGroupChannels(i, whistleControl.whistleParameters.channelBitmap, 
						whistleControl.whistleParameters.channelGroups);
				peakDetectors[i].setGroupChannels(groupChannels);
				whistleLinkers[i].setGroupChannels(groupChannels);
			}
		}

		whistleDataBlock.setLocalisationContents(0);

		int groupChannels;
		for (int i = 0; i < nChannelGroups; i++) {
			groupChannels = GroupedSourcePanel.getGroupChannels(i, whistleControl.whistleParameters.channelBitmap, 
					whistleControl.whistleParameters.channelGroups);
			
			if (PamUtils.getNumChannels(groupChannels) > 1 && fftDataBlock.getSequenceMapObject()==null) {
				whistleDataBlock.addLocalisationContents(LocContents.HAS_BEARING);
			}
		}
	}
	
	synchronized private void destroyDetectors() {
		if (peakDetectors == null) {
			return;
		}
		for (int i = 0; i < peakDetectors.length; i++) {
			removeOutputDatablock(peakDetectors[i].peakDataBlock);
			removeOutputDatablock(whistleLinkers[i].getOutputDataBlock(0));
			peakDetectors[i].destroyProcess();
			whistleLinkers[i].destroyProcess();
			whistleControl.removePamProcess(peakDetectors[i]);
			whistleControl.removePamProcess(whistleLinkers[i]);
		}
		peakDetectors = null;
		whistleLinkers = null;
		
	}

	@Override
	public void destroyProcess() {
		destroyDetectors();
		super.destroyProcess();
	}

	/*
	 * Methods from PamProcess and PamObserver which must be implemented
	 */
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		if (peakDetectors == null) return;
		for (int i = 0; i < peakDetectors.length; i++) {
			peakDetectors[i].setSampleRate(sampleRate, false);
			whistleLinkers[i].setSampleRate(sampleRate, false);
		}
	}

	@Override
	public void newData(PamObservable obs, PamDataUnit obj) {
		// Should get called each time there is a new DataUnit from an FFT
		// Process
		FFTDataUnit fftUnit = (FFTDataUnit) obj;
		currentSample = fftUnit.getStartSample();
//		send it to the appropriate detector. 

		if (peakDetectors != null) for (int i = 0; i < peakDetectors.length; i++) {
//			if ((peakDetectors[i].getGroupChannels() & obj.getChannelBitmap()) != 0) {
			if ((peakDetectors[i].getGroupChannels() & obj.getSequenceBitmap()) != 0) {
				peakDetectors[i].addData(obs, obj); // save the peak detector having to suscribe itself.
				prepareEqualisedSpectrumData((FFTDataUnit) obj, peakDetectors[i].getEqualisationConstants());
			}
		}
	}
	
	private void prepareEqualisedSpectrumData(FFTDataUnit fftData, double[] background) {
		FFTDataUnit eDataUnit = supressedSpectrogram.getRecycledUnit();
		if (eDataUnit == null) {
			eDataUnit = new FFTDataUnit(fftData.getTimeMilliseconds(), fftData.getChannelBitmap(), 
					fftData.getStartSample(), fftData.getSampleDuration(), null, fftData.getFftSlice());
		}
		else {
			eDataUnit.setInfo(fftData.getTimeMilliseconds(), fftData.getChannelBitmap(), 
					fftData.getStartSample(), fftData.getSampleDuration(), fftData.getFftSlice());
		}
		eDataUnit.setSequenceBitmap(fftData.getSequenceBitmapObject());
		ComplexArray sourceData = fftData.getFftData();
		ComplexArray data = eDataUnit.getFftData();
		if (data == null || data.length() != sourceData.length()) {
			data = new ComplexArray(sourceData.length());
		}
		for (int i = 0; i < data.length(); i++) {
			data.setReal(i, Math.sqrt(Math.max(0, sourceData.magsq(i) - background[i])));
		}
		eDataUnit.setFftData(data);
		supressedSpectrogram.addPamData(eDataUnit);
	}

//	public long FirstRequiredTime(PamObservable obs, Object obj) {
//		return SamplesToMilliseconds(currentSample - (long) sampleRate);
//	}
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		// if bearings are being measured, make sure enough data are being kept
		// for the bearing calculation. 
//		long t1 = Math.max((long) 1000, peakDetector.getRequiredDataHistory(o, arg));
////		if (whistleControl.whistleParameters.measureBearings) {
//			long firstShapeStart = whistleLinker.getFirstShapeStart();
//			if (firstShapeStart > 0) {
//			  long t2 = PamCalendar.getTimeInMillis() - whistleLinker.getFirstShapeStart() + 1000;
//			  return Math.max(t1, t2);
//			}
//		}
		return 2000;
	}

	boolean newWhistleEmbryo(WhistleShape newWhistle, int groupChannels, int detectionChannel) {
		// return true if it gets promoted to whistledom
		int nPeaks = newWhistle.getSliceCount();
		boolean isGood = isGoodWhistle(newWhistle);
		if (!isGood) return false;
		
//		newWhistle.setDBAmplitude(daqProcess.fftAmplitude2dB(newWhistle.getAmplitude(), 
//				detectionChannel, getSampleRate(), fftLength, true, false));
		newWhistle.setDBAmplitude(daqProcess.fftAmplitude2dB(newWhistle.getAmplitude(), 
				PamUtils.getLowestChannel(fftDataBlock.getChannelMap()), getSampleRate(), fftLength, true, false));
		
		long startSample = newWhistle.GetPeak(0).startSample;
		long lastSample = newWhistle.getLastPeak().startSample;
//		PamDataUnit newDataUnit = whistleDataBlock.getNewUnit(startSample, lastSample
//				- startSample, 1 << iChan);
		ShapeDataUnit sdu = new ShapeDataUnit(absSamplesToMilliseconds(startSample), groupChannels, 
				startSample, (int)(lastSample-startSample), newWhistle);
		sdu.sortOutputMaps(fftDataBlock.getChannelMap(), fftDataBlock.getSequenceMapObject(), groupChannels);
		sdu.setParentDataBlock(whistleDataBlock); // need this in some of the loc stuff, soset it now

		// need to convert frequency bins to Hz.
		
		double[] f = new double[2];
		f[0] = binsToHz(newWhistle.getMinFreq());
		f[1] = binsToHz(newWhistle.getMaxFreq());
		sdu.setFrequency(f);

		// calculate bearings / delays if there is > 1 channel in the group and we aren't dealing with sequence numbers
		if (PamUtils.getNumChannels(groupChannels) > 1 && fftDataBlock.getSequenceMapObject()==null) {
			localiseWhistle(sdu);
		}
		
		/*
		 * If there are multiple channel groups, try to cross channel link to other
		 * whistles, then run localisation ...
		 */
		if (nChannelGroups > 1 && sdu.getLocalisation() != null) {
			ShapeDataUnit linkUnit = crossChannelLink(sdu);
			if (linkUnit != null && linkUnit.getLocalisation() != null) {
//				if (wgd.)
				WhistleGroupDetection wgd = new WhistleGroupDetection(linkUnit);
				wgd.addSubDetection(sdu);
				/*
				 *  and localise using crossed bearing...
				 * Either or these will return false if no cross point was 
				 * found. Write out if either are OK.
				 */
				boolean ok1 = detectionGroupLocaliser.localiseDetectionGroup(wgd, 1);
				boolean ok2 = detectionGroupLocaliser.localiseDetectionGroup(wgd, -1);
				if (ok1 || ok2) {
					whistleLocations.addPamData(wgd);
				}
			}
		}
		
		whistleDataBlock.addPamData(sdu);

		return true;
	}
	
	private boolean isGoodWhistle(WhistleShape newWhistle) {

		int nPeaks = newWhistle.getSliceCount();
		boolean debugOutput = false;
		if (nPeaks < whistleControl.whistleParameters.minLength){
			if (debugOutput) System.out.println(String.format("Whistle too short %d peaks", nPeaks));
			return false;
		}
		double occupancy = nPeaks * 100 / (newWhistle.getLastPeak().getSliceNo() - newWhistle.GetPeak(0).getSliceNo());
		if (occupancy < whistleControl.whistleParameters.minOccupancy){
			if (debugOutput) System.out.println("Whistle low occupancy");
			return false;
		}
		if (newWhistle.nSoundClash > 3){
			if (debugOutput) System.out.println("Whistle sound clash");
			return false;
		}

		if (debugOutput) System.out.println("Whistle perfect");
		
		return true;
	}
	
	/**
	 * sorts out the bearing(s) for a single whistle (any number of close together channels)
	 * @param shapeDataUnit
	 * @return
	 */
	private boolean localiseWhistle(ShapeDataUnit shapeDataUnit) {

		int nChannels = PamUtils.getNumChannels(shapeDataUnit.getChannelBitmap());
		if (nChannels < 2) {
			return false;
		}
		
		int hydrophoneList = ((AcquisitionProcess) getSourceProcess()).
		getAcquisitionControl().ChannelsToHydrophones(shapeDataUnit.getChannelBitmap());
		
		WhistleLocalisation whistleLocalisation = new WhistleLocalisation(shapeDataUnit, hydrophoneList);
		
		int delay, detectionChannel, bearingChannel;
		detectionChannel = PamUtils.getNthChannel(0, shapeDataUnit.getChannelBitmap());
		for (int i = 1; i < nChannels; i++) {
			// measure as many delays as possible, all relative to channel 0, which will
			// contain the detection data. 
			bearingChannel = PamUtils.getNthChannel(i, shapeDataUnit.getChannelBitmap());
			delay = whistleControl.whistleLocaliser.getDelay(fftDataBlock,
					shapeDataUnit.getWhistleShape(), detectionChannel, bearingChannel);
			whistleLocalisation.setDelay(delay, i-1);
			if (delay == WhistleLocaliser.WHISTLE_NODELAY) {
				return false;
			}
		}
//		if (whistleControl.whistleParameters.measureBearings) {
//			int delay = whistleControl.whistleLocaliser.getDelay(getParentDataBlock(),
//					newWhistle,
//					whistleControl.whistleParameters.detectionChannel, 
//					whistleControl.whistleParameters.bearingChannel);
//			newWhistle.delay = delay;
////			System.out.println("Whistle delay = " + delay);
//		}

		
		shapeDataUnit.setLocalisation(whistleLocalisation);
		return true;
	}
	
	ShapeDataUnit crossChannelLink(ShapeDataUnit newUnit) {
		// search through recent whistles from other channels and see if 
		// any can be linked to this new whistle.
		if (newUnit == null){
			System.out.println("Null newUnit in WhistleDetector.crossChannelLink");
			return null;
		}
		int nW = whistleDataBlock.getUnitsCount();
		ShapeDataUnit oldUnit;
		double oT, oF;
		ListIterator<ShapeDataUnit> wslIterator = whistleDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
		while (wslIterator.hasPrevious()) {
			oldUnit = wslIterator.previous();
			if (oldUnit == null){
				System.out.println("Null oldUnit in WhistleDetector.crossChannelLink");
				continue;
			}
			
//			if ((oldUnit.getChannelBitmap() & newUnit.getChannelBitmap()) != 0) {
			if ((oldUnit.getSequenceBitmap() & newUnit.getSequenceBitmap()) != 0) {
				continue;
			}
			// see if they overlap in time and frequency sufficiently. 
			oT = getTOverlap(newUnit, oldUnit);
			oF = getFOverlap(newUnit, oldUnit);
			if (oF > 0.5 && oT > 0.5) {
				return oldUnit;
			}
			if (oldUnit.getTimeMilliseconds() < newUnit.getTimeMilliseconds() - 20000) {
				break;
			}
		}
		
		return null;
	}
	
	double getTOverlap(ShapeDataUnit w1, ShapeDataUnit w2) {
		return Math.max(w1.getTimeOverlap(w2), w2.getTimeOverlap(w1));
	}
	
	double getFOverlap(ShapeDataUnit w1, ShapeDataUnit w2) {
		return Math.max(w1.getFrequencyOverlap(w2), w2.getFrequencyOverlap(w1));
	}
	

	@Override
	public void prepareProcess() {

		super.prepareProcess();
		
		fftDataBlock = (FFTDataBlock) PamController.getInstance().getFFTDataBlock(
				whistleControl.whistleParameters.fftDataName);


		// need to call this explicitly for the peam detector since it's not in
		// the main process list
		// get the right input data block
//		if (getParentDataBlock() != null) {
//			getParentDataBlock().deleteObserver(this);
//		}
		
		if (fftDataBlock == null) {
			fftDataBlock = (FFTDataBlock) PamController.getInstance().getFFTDataBlock(0);
			if (fftDataBlock != null) {
				whistleControl.whistleParameters.fftDataName = fftDataBlock.toString();
			}
		}
		
		if (fftDataBlock == null) {
			return;
		}
		setParentDataBlock(fftDataBlock);
		supressedSpectrogram.setFftHop(fftDataBlock.getFftHop());
		supressedSpectrogram.setFftLength(fftDataBlock.getFftLength());
		whistleDataBlock.setFftHop(fftDataBlock.getFftHop());
		whistleDataBlock.setFftLength(fftDataBlock.getFftLength());
		whistleDataBlock.sortOutputMaps(fftDataBlock.getChannelMap(), fftDataBlock.getSequenceMapObject(), whistleControl.whistleParameters.channelBitmap);
		whistleLocations.sortOutputMaps(fftDataBlock.getChannelMap(), fftDataBlock.getSequenceMapObject(), whistleControl.whistleParameters.channelBitmap);

		
		// also need to make the event detector observe the same block
		getParentDataBlock().addObserver(whistleControl.eventDetector);
		
//		GetSourceDataBlock().addObserver(this);
		
		
		fftLength = fftDataBlock.getFftLength();
		fftHop = fftDataBlock.getFftHop();
		setSampleRate(fftDataBlock.getSampleRate(), false);
		/*maxDF = whistleControl.whistleParameters.maxDF * fftHop / sampleRate;
		maxDF = Math.pow(2, maxDF);
		maxD2F = whistleControl.whistleParameters.maxD2F * fftHop / sampleRate;
		maxD2F = Math.pow(2, maxD2F);*/
		try {
		daqProcess = (AcquisitionProcess) getSourceProcess();
		}
		catch (ClassCastException ex) {
			return ;
		}

		int supChannels = 0;
		if (peakDetectors != null) for (int i = 0; i < peakDetectors.length; i++) {
			peakDetectors[i].prepareProcess();
			supChannels |= (1<<peakDetectors[i].getDetectionChannel());
		}
		
//		supressedSpectrogram.setChannelMap(supChannels);
		supressedSpectrogram.sortOutputMaps(fftDataBlock.getChannelMap(), fftDataBlock.getSequenceMapObject(), supChannels);

		buildDetectors();
		
		// System.out.println("In whistle detector prep process");
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {

	}

	/**
	 * Convert a number of frequency bins in the spectrgram matrix to a 
	 * frequency in Hz.
	 * @param nFrequencyBins number of frequency bins
	 * @return frequency in Hz
	 */
	public double binsToHz(int nFrequencyBins) {
		return (double) nFrequencyBins * getSampleRate() / fftLength;
	}
	
	public int hzToBins(double frequency) {
		int bin = (int) (frequency * fftLength / getSampleRate());
		bin = Math.max(bin, 0);
		bin = Math.min(bin, fftLength/2-1);
		return bin;
	}
	
	/**
	 * Convert a number of time bins in the spectrgram matris to a
	 * a time in seconds
	 * @param nTimeBins number of time bins
	 * @return time in seconds
	 */
	public double binsToSeconds(int nTimeBins) {
		return (double) nTimeBins * fftHop / getSampleRate();
	}
//
//	public FFTDataSource getParentFFTDataSource() {
//		return parentFFTDataSource;
//	}

	public int getFftHop() {
		if (fftDataBlock == null) return 0;
		return fftDataBlock.getFftHop();
	}

	public int getFftLength() {
		if (fftDataBlock == null) return 0;
		return fftDataBlock.getFftLength();
	}

	public WhistleControl getWhistleControl() {
		return whistleControl;
	}

	public FFTDataBlock getFftDataBlock() {
		return fftDataBlock;
	}
}
