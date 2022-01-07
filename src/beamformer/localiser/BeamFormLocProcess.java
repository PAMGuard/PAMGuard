package beamformer.localiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import Array.ArrayManager;
import Array.PamArray;
import Localiser.algorithms.PeakPosition;
import Localiser.algorithms.PeakPosition2D;
import Localiser.algorithms.PeakSearch;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataUnavailableException;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamFormerParams;
import beamformer.BeamGroupProcess;
import beamformer.annotation.BeamFormerAnnotation;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.loc.BeamFormerLocalisation;
import crossedbearinglocaliser.TempDataUnit;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FFTParameters;
import fftManager.PamFFTControl;
import fftManager.PamFFTProcess;

/**
 * This process doesn't calculate continuous beams, but when data do arrive, 
 * it calculates a beam'O'Gram for data in that time-frequency box and estimates 
 * a best location based on that time interval.  
 * @author Doug Gillespie
 *
 */
public class BeamFormLocProcess extends BeamFormerBaseProcess {

	private ArrayList<BeamOGroup> collatedBeamData;

	private List<BeamOGramDataUnit> collatedBeamOGram = new ArrayList<>();

	private List<FFTDataUnit> collatedFFTData;

	private BeamFormLocaliserControl beamFormerLocaliserControl;

	private PeakSearch peakSearch;

	private PamFFTControl privateFFTControl;

	private FFTDataBlock fftSourceData;

	private enum InputTypes {FFTData, RawData, FFTDataHolder, RAWDataHolder};

	private InputTypes inputType;
	
	private BFLDataOutput bflDataOutput;

	/**
	 * A dummy raw data block for beam forming raw audio data. 
	 */
	private PamRawDataBlock dummyRawDataBlock;

	public BeamFormLocProcess(BeamFormLocaliserControl beamFormerLocaliserControl) {
		super(beamFormerLocaliserControl, false);
		this.beamFormerLocaliserControl = beamFormerLocaliserControl;
		getBeamOGramOutput().addObserver(new BeamDataMonitor(), false);
		collatedBeamData = new ArrayList<>();
		collatedFFTData = new ArrayList<>();
		peakSearch = new PeakSearch(true);
		this.addOutputDataBlock(bflDataOutput = new BFLDataOutput(beamFormerLocaliserControl, this, beamFormerLocaliserControl.getUnitName() + " output"));
//		bflDataOutput.setOverlayDraw(new PamDetectionOverlayGraphics(bflDataOutput, 
//				new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLUE, Color.GREEN)));
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		// sort out additional FFT source !
		BeamFormerParams params = beamFormerLocaliserControl.getBeamFormerParams();
		PamDataBlock<?> beamSource = PamController.getInstance().getDataBlockByLongName(params.getDataSource());	

		if (fftSourceData != null) {
			if (fftSourceData.getParentProcess() != null) {
				fftSourceData.getParentProcess().removeObservable(fftSourceData);
			}
			fftSourceData = null;
		}
		if (dummyRawDataBlock != null) {
			if (dummyRawDataBlock.getParentProcess() != null) {
				dummyRawDataBlock.getParentProcess().removeObservable(fftSourceData);
			}
			dummyRawDataBlock = null;
		}

		if (beamSource == null) {
			inputType = null;
			return;
		}
		if (beamSource instanceof PamRawDataBlock) {
			inputType = InputTypes.RawData;
			prepareRawInput((PamRawDataBlock) beamSource);
		}
		else if (beamSource instanceof FFTDataBlock) {
			inputType = InputTypes.FFTData;
			prepareFFTInput((FFTDataBlock) beamSource);
		}
		else if (FFTDataHolder.class.isAssignableFrom(beamSource.getUnitClass())) {
			inputType = InputTypes.FFTDataHolder;
			prepareFFTHolderInput(beamSource);
		}
		else if (RawDataHolder.class.isAssignableFrom(beamSource.getUnitClass())) {
			inputType = InputTypes.RAWDataHolder;
			prepareRawHolderInput(beamSource);
		}
		else {
			inputType = null;
			System.err.println("Unidentified and unusable beam data source: " + beamSource.getLongDataName());
			return;
		}

		super.prepareProcess();

		collatedBeamData.clear();
		ArrayList<BeamGroupProcess> groupProcesses = this.getGroupProcesses();
		if (groupProcesses == null) {
			return;
		}
		for (BeamGroupProcess aProcess:groupProcesses) {
			aProcess.getBeamFormerAlgorithm().setKeepFrequencyInformation(true);
			collatedBeamData.add(new BeamOGroup(aProcess));
		}
	}

	/**
	 * Get ready for normal FFT input to the beam former
	 * @param beamSource
	 */
	private void prepareFFTInput(FFTDataBlock beamSource) {
		this.fftSourceData = beamSource;
		dummyRawDataBlock = null;
	}

	/**
	 * Set up a FFT engine to convert raw data to FFT chunks for the 
	 * beam former. All based around an entire FFT controlled unit that
	 * doesn't ever get registered with the system. However, need to bodge things
	 * slightly so that the FFT module knows that its source is the raw data block, 
	 * but it doesn't actually subscribe to it and receive FFT data. 
	 * @param beamSource
	 */
	private void prepareRawInput(PamRawDataBlock beamSource) {
		if (privateFFTControl == null) {
			privateFFTControl = new PamFFTControl("Private FFT for beamformer");
			privateFFTControl.getPamProcess(0).getOutputDataBlock(0).addObserver(new PrivateFFTMonitor(), false);
		}
		BFLocaliserParams bfParams = (BFLocaliserParams) beamFormerLocaliserControl.getBeamFormerParams();	
		dummyRawDataBlock = beamSource;
		FFTParameters fftParams = privateFFTControl.getFftParameters();
		fftParams.fftLength = bfParams.fftLength;
		fftParams.fftHop = bfParams.fftHop;
		fftParams.dataSourceName = beamSource.getDataName();
		fftParams.channelMap = bfParams.getChannelBitmap();
		privateFFTControl.getPamProcess(0).prepareProcess();
		// now try to unsubscribe the process from the beamSource. 
		beamSource.deleteObserver(privateFFTControl.getPamProcess(0));
		this.fftSourceData = (FFTDataBlock) privateFFTControl.getPamProcess(0).getOutputDataBlock(0);
	}

	/**
	 * Prepare FFT source when the FFT data will be available from the incoming
	 * data (currently only applies to click detector). Will require a 
	 * dummy FFT data block that doesn't actually do anything. 
	 * 
	 * @param beamSource
	 */
	private void prepareFFTHolderInput(PamDataBlock<?> beamSource) {
		/**
		 * We need to find a source of raw data to stick ahead of this thing so that it can 
		 * server the correct gains and hydrophone positions this can be easily achieved by just giving 
		 * it the detection monitor process which should work through.
		 */
		BFLocaliserParams bfParams = (BFLocaliserParams) beamFormerLocaliserControl.getBeamFormerParams();
		BFDetectionMonitor parentProcess = beamFormerLocaliserControl.getBfDetectionMonitor();
		fftSourceData = new FFTDataBlock("Beam localisation Data", parentProcess, bfParams.getChannelBitmap(), 
				bfParams.fftHop, bfParams.fftLength);
		if (parentProcess != null) {
			fftSourceData.setSampleRate(parentProcess.getSampleRate(), false);
		}
		dummyRawDataBlock = null;
	}

	/**
	 * Prepare for when raw data are in the data unit, may be one or more FT chunks. 
	 * Use the full FFT system as for other raw data, but when processing, we'll have to 
	 * chunk it up and chuck it in in RawDataunits. Will also have to change the 
	 * data source for the process - will it crash if it isn't raw data !
	 * @param beamSource
	 */
	private void prepareRawHolderInput(PamDataBlock<?> beamSource) {
		BFDetectionMonitor parentProcess = beamFormerLocaliserControl.getBfDetectionMonitor();
		if (privateFFTControl == null) {
			privateFFTControl = new PamFFTControl("Private FFT for beamformer");
			privateFFTControl.getPamProcess(0).getOutputDataBlock(0).addObserver(new PrivateFFTMonitor(), false);
		}
		BFLocaliserParams bfParams = (BFLocaliserParams) beamFormerLocaliserControl.getBeamFormerParams();	
		dummyRawDataBlock = new PamRawDataBlock("Beamformer Raw Data Input", parentProcess, bfParams.getChannelBitmap(), parentProcess.getSampleRate());	
		FFTParameters fftParams = privateFFTControl.getFftParameters();
		fftParams.fftLength = bfParams.fftLength;
		fftParams.fftHop = bfParams.fftHop;
		fftParams.dataSourceName = dummyRawDataBlock.getDataName();
		fftParams.channelMap = bfParams.getChannelBitmap();
		/**
		 * It should fail to find it's raw data during this process, but that shouldn't matter. 
		 */
		privateFFTControl.getPamProcess(0).prepareProcess();
		// now try to unsubscribe the process from the beamSource. 
		privateFFTControl.getPamProcess(0).setParentDataBlock(dummyRawDataBlock);

		this.fftSourceData = (FFTDataBlock) privateFFTControl.getPamProcess(0).getOutputDataBlock(0);

	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseProcess#getFftDataSource()
	 */
	@Override
	public FFTDataBlock findFFTDataBlock() {
		return fftSourceData;
	}

	@Override
	public void pamStart() {

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object)
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		/**
		 * Probably need to be smarter about this - especially when users are marking on a display, which 
		 * may be quite a long time after a sound has passed, however if we're beam forming on 
		 * automatic detectors, then there is no need to store anything beyond the length of a detection.
		 * Also, there is no need to store if the raw or fft data is held by the data unit.   
		 */
		switch (inputType) {
		case FFTData:
			return 60*1000;
		case FFTDataHolder:
			return 0;	
		case RAWDataHolder:
			return 0;	
		case RawData:
			return 60*1000;
		default:
			return 0;		
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		// nothing to do here. All happens when detections arrive ...
	}
	/**
	 * Run the beam former within the given time-frequency box. 
	 * @param timeRange
	 * @param freqRange
	 * @param markChannels
	 */
	synchronized public boolean beamFormDataUnit(PamDataUnit pamDataUnit) {
		/**
		 * Each data unit is beam formed on ONE channel group 
		 * only. Therefore need to find out which BF group it's in 
		 * at the start and only work with those channels all the way through 
		 * processing.
		 */
		BeamOGroup beamGroup = findBeamOGroup(pamDataUnit.getChannelBitmap());
		if (beamGroup == null) {
			System.err.printf("Unable to find beam group for channels 0x%0x in %s\n", pamDataUnit.getChannelBitmap(), pamDataUnit.toString());
			return false;
		}
		if (beamFormerLocaliserControl.getBfLocaliserParams().doAllGroups) {
			/*
			 *  make temporary data units and beam form them first so that if all these 
			 *  end up with a crossed bearing localiser, then the last to arrive is 
			 *  the real data unit, not the dummies.  
			 */
			for (BeamOGroup bg:collatedBeamData) {
				if (bg == beamGroup) {
					continue;
				}
				TempDataUnit tbdu = new TempDataUnit(pamDataUnit, bg.channelMap);
				beamFormGroup(tbdu, bg);
			}
		}
		
		return beamFormGroup(pamDataUnit, beamGroup);
	}
	
	/**
	 * Beam for a data unit within a particular beam group. 
	 * @param pamDataUnit data unit
	 * @param beamGroup beam group
	 * @return true if successful
	 */
	public boolean beamFormGroup(PamDataUnit pamDataUnit, BeamOGroup beamGroup) {
		BeamFormerLocalisation bfLocalisation = null;
		switch (inputType) {
		case FFTData:
			bfLocalisation = beamFormFFTData(pamDataUnit, beamGroup);
			break;
		case FFTDataHolder:
			bfLocalisation =  beamFormFFTDataHolder(pamDataUnit, beamGroup);
			break;
		case RAWDataHolder:
			bfLocalisation =  beamFormRawDataHolder(pamDataUnit, beamGroup);
			break;
		case RawData:
			bfLocalisation =  beamFormRawData(pamDataUnit, beamGroup);
			break;
		default:
			return false;		
		}
		if (bfLocalisation != null) {
			//			pamDataUnit.setLocalisation(bfLocalisation);
		}
		return (bfLocalisation != null);
	}

	/**
	 * Beam form by taking FFT data from an FFT data source and 
	 * beam forming it. 
	 * @param pamDataUnit trigger dat aunit
	 * @return 
	 */
	private BeamFormerLocalisation beamFormFFTData(PamDataUnit pamDataUnit, BeamOGroup beamGroup) {
		/**
		 * Function works by getting an iterator from the source datablock, then pushing those data
		 * into the beam form algorithms exactly as they would have been in real time. An observer monitors 
		 * the beamogram output block (which is now invisible to the rest of PAMGuard) and collates
		 * all the beamogram outputs into an array list. Appropriate information can then be extracted
		 * from that summary list, displays filled, etc.  
		 */
		long t1 = pamDataUnit.getTimeMilliseconds();
		long t2 = pamDataUnit.getEndTimeInMilliseconds();
		double[] freqRange = pamDataUnit.getFrequency();
		if (freqRange == null) {
			return null;
		}
		//		System.out.printf("Beam Form from %s to %s, between %s\n", PamCalendar.formatTime(pamDataUnit.getTimeMilliseconds(), true), 
		//				PamCalendar.formatTime(t2, true), FrequencyFormat.formatFrequencyRange(freqRange, true));

		/*
		 * Are going to need to be a bit cleverer here since when firing off the click detector, the
		 * FFT data may not be there ! So need to queue up the new box and act only when the data have actually arrived. 
		 */

		collatedFFTData.clear();


		int[] channelGroupLut = getChannelGroupLUT();
		if (channelGroupLut == null) {
			return null;
		}
		/*
		 *  need to iterate through FFT data for the given time intervals and send them 
		 *  off to the process... 
		 */
		FFTDataBlock fftDataSource = getFftDataSource();
		if (fftDataSource == null) {
			return null;
		}
		int groupChannelMap = beamGroup.channelMap;
		int firstChannelMap = 1<<PamUtils.getLowestChannel(groupChannelMap);
		synchronized (fftDataSource.getSynchLock()) {
			ListIterator<FFTDataUnit> fftIterator = fftDataSource.getListIterator((long) t1, firstChannelMap, PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
			if (fftIterator == null) {
				System.out.println("FFT iterator unavailable ");
				return null;
			}
			FFTDataUnit firstUnit = null, lastUnit = null;
			boolean goodtoUse = false; // need to check we really are starting on the first channel or all will go wrong. 
			while (fftIterator.hasNext()) {
				FFTDataUnit fftData = fftIterator.next();
				if ((groupChannelMap & fftData.getChannelBitmap()) == 0) {
					continue;
				}
				if (fftData.getTimeMilliseconds() > t2) {
					break;
				}
				if (fftData.getChannelBitmap() == firstChannelMap) {
					goodtoUse = true;
				}
				if (goodtoUse == false) continue;
				if (firstUnit == null) firstUnit = fftData;
				lastUnit = fftData;
				collatedFFTData.add(fftData);
				/**
				 * Send the FFT data into the beam former. 
				 */
				//				System.out.printf("FFT Data sample %d time %s channel 0x%X\n", fftData.getStartSample(), 
				//						PamCalendar.formatTime(fftData.getTimeMilliseconds(), true), fftData.getChannelBitmap());
			}
			if (firstUnit == null) {
				return null;
			}
			//			if (firstUnit != null) {
			//				System.out.printf("Loaded %d datas from %s channel 0x%X to %s channel 0x%X\n", collatedFFTData.size(),
			//						PamCalendar.formatTime(firstUnit.getTimeMilliseconds(), true), firstUnit.getChannelBitmap(),
			//						PamCalendar.formatTime(lastUnit.getTimeMilliseconds(), true), lastUnit.getChannelBitmap());
			//			}
		}
		return beamFormFFTData(pamDataUnit, beamGroup, collatedFFTData);
	}

	/**
	 * Beam form raw data extracted back from an original raw data 
	 * audio source
	 * @param pamDataUnit
	 * @return
	 */
	private BeamFormerLocalisation beamFormRawData(PamDataUnit pamDataUnit, BeamOGroup beamGroup) {
		long t1 = pamDataUnit.getTimeMilliseconds();
		long t2 = pamDataUnit.getEndTimeInMilliseconds();
		PamRawDataBlock rawSourceData = null;
		try {
			rawSourceData = (PamRawDataBlock) getParentDataBlock();
		}
		catch (ClassCastException e) {
			System.err.println("Not raw data input in beam fomer: " + e.getMessage());
			return null;
		}
		BFLocaliserParams bfParams = (BFLocaliserParams) beamFormerLocaliserControl.getBeamFormerParams();	
		double[][] rawData = null;
		try {
			rawData = rawSourceData.getSamplesForMillis(t1, t2-t1, beamGroup.channelMap);
		} catch (RawDataUnavailableException e) {
			System.err.println("Not raw data available in beam fomer: " + e.getMessage());
			return null;
		}
		return beamFormRawData(pamDataUnit, beamGroup, rawData, bfParams.getChannelBitmap());
	}

	private BeamFormerLocalisation beamFormFFTDataHolder(PamDataUnit pamDataUnit, BeamOGroup beamGroup) {
		if (pamDataUnit instanceof FFTDataHolder) {
			FFTDataBlock fftDataSource = getFftDataSource();
			FFTDataHolder fftDataHolder = (FFTDataHolder) pamDataUnit;
			List<FFTDataUnit> fftDatas = fftDataHolder.getFFTDataUnits(fftDataSource.getFftLength());
			if (fftDatas != null && fftDatas.size() >= PamUtils.getNumChannels(pamDataUnit.getChannelBitmap())) {
				return beamFormFFTData(pamDataUnit, beamGroup, fftDatas);
			}
		}
		return null;
	}

	/**
	 * Beam form clips of raw data held within a data unit. 
	 * @param pamDataUnit
	 * @return
	 */
	private BeamFormerLocalisation beamFormRawDataHolder(PamDataUnit pamDataUnit, BeamOGroup beamGroup) {
		if (pamDataUnit instanceof RawDataHolder) {
			RawDataHolder rawDataHolder = (RawDataHolder) pamDataUnit;
			double[][] wavData = rawDataHolder.getWaveData();
			if (wavData != null && wavData.length == PamUtils.getNumChannels(pamDataUnit.getChannelBitmap())) {
				return beamFormRawData(pamDataUnit, beamGroup, wavData, pamDataUnit.getChannelBitmap());
			}
		}
		return null;
	}

	private BeamFormerLocalisation beamFormRawData(PamDataUnit pamDataUnit, BeamOGroup beamGroup, double[][] wavData, int channelMap) {
		FFTDataBlock fftDataSource = getFftDataSource();
		int fftLength = fftDataSource.getFftLength();
		int fftHop = fftDataSource.getFftHop();
		int nWavChan = wavData.length;
		int nMapChan = PamUtils.getNumChannels(channelMap);
		if (nWavChan != nMapChan) {
			System.err.printf("Beamformer channel number mismatch: %d in wave and %d in channel map for %s\n", 
					nWavChan, nMapChan, pamDataUnit);
			return null;
		}
		if (nWavChan == 0) {
			return null;
		}
		int nSamples = wavData[0].length;
		/*
		 * Check the length of the raw data, rounding it up to the next hop unless it's already >>
		 * several bins.  
		 */
		double dFFT = (nSamples - fftLength) / fftHop + 1;
		if (dFFT < 4) { // consider padding the data up to a round number of FFT's. 
			int nFFT = Math.max((int) Math.ceil(dFFT), 1);
			int wantedSamp = (nFFT-1) * fftHop + fftLength;
			if (wantedSamp > nSamples) {
				// pad the data, copying the entire array (avoids modifying internal data in the click detector). 
				wavData = Arrays.copyOf(wavData, wavData.length);
				for (int i = 0; i < wavData.length; i++) {
					wavData[i] = Arrays.copyOf(wavData[i], wantedSamp);
				}
			}
		}
		// find the FFT process
		PamFFTProcess fftProcess = (PamFFTProcess) privateFFTControl.getPamProcess(0);
		fftProcess.clearTempStores();
		collatedFFTData.clear();
		/*
		 * Now create raw data units to send to the fft module, everything should
		 * then happen quite automatically. 
		 */
		for (int i = 0; i < nWavChan; i++) {
			int iChan = PamUtils.getNthChannel(i, channelMap);
			RawDataUnit rawDataUnit = new RawDataUnit(pamDataUnit.getTimeMilliseconds(), 1<<iChan, 0, wavData[i].length);
			rawDataUnit.setRawData(wavData[i], false);
			fftProcess.addData(null, rawDataUnit);
		}
		return beamFormFFTData(pamDataUnit, beamGroup, collatedFFTData);
	}

	private BeamFormerLocalisation beamFormFFTData(PamDataUnit pamDataUnit, BeamOGroup beamGroup, List<FFTDataUnit> fftDataList) {

		FFTDataBlock fftDataSource = getFftDataSource();

		collatedBeamOGram.clear();
		
		beamGroup.groupProcess.resetFFTStore();

		/**
		 * Set the frequency range for analysis
		 */
		double[] fRange = pamDataUnit.getFrequency();
		if (fRange != null && fRange.length >= 2) {
			int[] binRange = frequencyToBin(fRange);
			beamGroup.groupProcess.getBeamFormerAlgorithm().setFrequencyBinRange(binRange[0], binRange[1]);
		}
		/*
		 * Now see if the data unit has more detailed contour information that
		 * we might use ...
		 */
		TFContourData contourData = null;
		int nContourCont = 0;
		if (pamDataUnit instanceof TFContourProvider) {
			TFContourProvider cp = (TFContourProvider) pamDataUnit;
			contourData = cp.getTFContourData();
			if (contourData != null) {
				nContourCont = contourData.getContourTimes().length;
				// check if it's simple to match the contour info with the FFT data:
//				int nFFT = fftDataList.size() / PamUtils.getNumChannels(beamGroup.channelMap);
//				System.out.printf("Contour with %d slices for %d FFT datas dt1 = %d\n", 
//						contourData.getContourTimes().length, nFFT, fftDataList.get(0).getTimeMilliseconds()-contourData.getContourTimes()[0]);
			}

		}

		int iGroupCount = 0;
		int firstChannel = fftDataList.get(0).getChannelBitmap();
		for (FFTDataUnit fftDataUnit:fftDataList) {
			fftDataUnit.setParentDataBlock(fftDataSource);
			if (iGroupCount < nContourCont && fftDataUnit.getChannelBitmap() == firstChannel) {
				int binLo = frequencyToBin(contourData.getLowFrequency()[iGroupCount]);
				int binHi = frequencyToBin(contourData.getHighFrequecy()[iGroupCount])+1;
				binHi = Math.min(binHi, fftDataUnit.getFftData().length());
				beamGroup.groupProcess.getBeamFormerAlgorithm().setFrequencyBinRange(binLo, binHi);
				iGroupCount++;
			}
			makeContinuousBeams(fftDataSource, fftDataUnit);
		}

		//		System.out.printf("%d collated beam ogram data units available\n", collatedBeamOGram.size());
		if (collatedBeamOGram.size() == 0) {
			return null;
		}

		double[] beamAngles = null;
		if (beamGroup.nDimensions == 1) {
			beamAngles = interpret1DBeamOGram(collatedBeamOGram, beamGroup);
		}
		else if (beamGroup.nDimensions == 2) {
			beamAngles = interpret2DBeamOGram(collatedBeamOGram, beamGroup);
		} 
		//		angle1Data = 

		BeamLocaliserData bld = new BeamLocaliserData(pamDataUnit.getTimeMilliseconds(), fftSourceData, getBeamOGramOutput(), 
				fftDataList, collatedBeamOGram, pamDataUnit.getFrequency(), beamAngles);
		bld.setChannelBitmap(beamGroup.channelMap);

		//		double[] angle1Data = BeamOGramDataUnit.getAverageAngle1Data(collatedBeamOGram);
		//		// now collapse that 
		//		PeakPosition peakPosition = peakSearch.interpolatedPeakSearch(angle1Data);
		//		BeamOGramDataUnit stBG = collatedBeamOGram.get(0);
		//		int chanMap = stBG.getChannelBitmap();
		//		//		angle1Data = 
		//
		//		BeamLocaliserData bld = new BeamLocaliserData(pamDataUnit.getTimeMilliseconds(), fftDataSource, getBeamOGramOutput(), 
		//				fftDataList, collatedBeamOGram, pamDataUnit.getFrequency(), angle1Data);
		//		bld.setChannelBitmap(chanMap);

		beamFormerLocaliserControl.getBeamLocaliserObservable().update(bld);

		AbstractLocalisation currentLoc = pamDataUnit.getLocalisation();
		//		if (currentLoc != null) {
		//			double[] angles = currentLoc.getAngles();
		//			if (angles != null) {
		//				System.out.printf("Updated %s angle from %3.3f to %3.3f\n", pamDataUnit.getClass().getName(), 
		//						Math.toDegrees(angles[0]), Math.toDegrees(beamAngles[0]));
		//			}
		//		}
		BFLocaliserParams bfParams = (BFLocaliserParams) beamFormerLocaliserControl.getBeamFormerParams();	
		if (beamGroup.arrayShape == ArrayManager.ARRAY_TYPE_VOLUME) {
			beamAngles[0] = Math.PI/2. - beamAngles[0];
		}
		BeamFormerLocalisation bfLocalisation = new BeamFormerLocalisation(pamDataUnit, beamGroup.locContents, bfParams.getChannelBitmap(), beamAngles, 0);
		bfLocalisation.setSubArrayType(beamGroup.arrayShape);
		bfLocalisation.setArrayAxis(beamGroup.groupProcess.getArrayMainAxes());
		double[] angleErrors = new double[beamAngles.length];
		for (int i = 0; i < angleErrors.length; i++) {
			angleErrors[i] = Math.toRadians(1.);
		}
		pamDataUnit.setLocalisation(bfLocalisation);
		pamDataUnit.addDataAnnotation(new BeamFormerAnnotation(beamFormerLocaliserControl.getBfAnnotationType(), bfLocalisation));
		
		/**
		 * Put the original data unit into an output data block. this is needed so that 
		 * the beam former can make secondary data units, beam forming on all channel groups. 
		 */
		bflDataOutput.addPamData(pamDataUnit);
		
		return bfLocalisation;
	}

	private double[] interpret2DBeamOGram(List<BeamOGramDataUnit> collatedBeamOGram, BeamOGroup beamGroup) {
		double[][] angleData = BeamOGramDataUnit.averageAngleAngleData(collatedBeamOGram);
		peakSearch.setWrapDim0(true);
		peakSearch.setWrapStep0(2);
		PeakPosition2D peakPosition = peakSearch.interpolatedPeakSearch(angleData);
		int[] angRange = beamGroup.algoParams.getBeamOGramAngles();
		int[] slantRange = beamGroup.algoParams.getBeamOGramSlants();
		double ang0 = peakPosition.getBin0() * angRange[2] + angRange[0];
		double ang1 = peakPosition.getBin1() * slantRange[2] + slantRange[0];
		double[] ang = {Math.toRadians(ang0), Math.toRadians(ang1)};
		return ang;
	}

	private double[] interpret1DBeamOGram(List<BeamOGramDataUnit> collatedBeamOGram2, BeamOGroup beamGroup) {
		double[] angle1Data = BeamOGramDataUnit.getAverageAngle1Data(collatedBeamOGram);
		// now collapse that 
		peakSearch.setWrapDim0(false);
		PeakPosition peakPosition = peakSearch.interpolatedPeakSearch(angle1Data);
		int[] angRange = beamGroup.algoParams.getBeamOGramAngles();
		double[] ang = {Math.toRadians(peakPosition.getBin() * angRange[2] + angRange[0])};
		return ang;
	}

	/**
	 * Class to collate information from channel groups. 
	 * @author Doug Gillespie
	 *
	 */
	private class BeamOGroup {

		private BeamGroupProcess groupProcess;

		private int channelMap;

		private BeamAlgorithmParams algoParams;

		private int nDimensions;

		private int locContents;

		private int arrayShape;

		public BeamOGroup(BeamGroupProcess aProcess) {
			this.groupProcess = aProcess;
			this.channelMap = groupProcess.getAlgorithmParams().getChannelMap();
			algoParams = aProcess.getAlgorithmParams();
			int[] slants = algoParams.getBeamOGramSlants();
			nDimensions = 1;
			if (slants != null && slants.length >= 2) {
				if (slants[1] > slants[0]) {
					nDimensions = 2;
				}
			}
			if (nDimensions == 1) {
				locContents = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY | LocContents.HAS_BEARINGERROR;
			}
			else {
				locContents = LocContents.HAS_BEARING | LocContents.HAS_BEARINGERROR;
			}
			// work out the subArray Shape. 
			ArrayManager arrayManager = ArrayManager.getArrayManager();
			PamArray currentArray = arrayManager.getCurrentArray();
			int phones = channelMap;
			phones = getFftDataSource().getChannelListManager().channelIndexesToPhones(phones);
			arrayShape = arrayManager.getArrayShape(currentArray, phones);
		}


	}

	/**
	 * Find group output from a beamogram. 
	 * @param channelMap
	 * @return
	 */
	private BeamOGroup findBeamOGroup(int channelMap) {
		for (BeamOGroup bg:collatedBeamData) {
			if ((bg.channelMap & channelMap) != 0) {
				return bg;
			}
		}
		return null;
	}

	private class PrivateFFTMonitor extends PamObserverAdapter {

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			collatedFFTData.add((FFTDataUnit) dataUnit);
		}

		@Override
		public String getObserverName() {
			return "Private Beamformer FFT Monitor";
		}
	}

	private class BeamDataMonitor extends PamObserverAdapter {

		/* (non-Javadoc)
		 * @see PamguardMVC.PamObserverAdapter#update(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			collatedBeamOGram.add((BeamOGramDataUnit) dataUnit);
		}

		@Override
		public String getObserverName() {
			return "Beam form localiser";
		}

	}

	/**
	 * @return the beamFormerLocaliserControl
	 */
	public BeamFormLocaliserControl getBeamFormerLocaliserControl() {
		return beamFormerLocaliserControl;
	}

}
