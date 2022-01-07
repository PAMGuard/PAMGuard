package bearinglocaliser.algorithms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import PamController.PamController;
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
import annotation.CentralAnnotationsList;
import bearinglocaliser.BearingAlgorithmGroup;
import bearinglocaliser.BearingLocalisation;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.DetectionMonitor;
import bearinglocaliser.annotation.BearingAnnotation;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FFTParameters;
import fftManager.PamFFTControl;
import fftManager.PamFFTProcess;
import fftManager.fftorganiser.FFTDataException;
import fftManager.fftorganiser.FFTDataList;
import fftManager.fftorganiser.FFTDataOrganiser;
import fftManager.fftorganiser.FFTInputTypes;
import fftManager.fftorganiser.OrganisedFFTData;

abstract public class BaseFFTBearingAlgorithm implements BearingAlgorithm {

	private BearingProcess bearingProcess;
	private BearingAlgorithmParams algorithmParams;
	private int groupIndex;
	private BearingLocaliserControl bearingLocaliserControl;
	
//	private PamFFTControl privateFFTControl;
	/**
	 * A dummy raw data block for beam forming raw audio data. 
	 */
//	private PamRawDataBlock dummyRawDataBlock;

//	private List<FFTDataUnit> collatedFFTData;

//	private enum InputTypes {FFTData, RawData, FFTDataHolder, RAWDataHolder};
//
	private FFTInputTypes inputType;
	
	private FFTDataOrganiser fftDataOrganiser;
	
//	private FFTDataBlock fftSourceData;
	private PamDataBlock detectionDataBlock;
	private PamDataBlock fftOrRawDataBlock;
	private FFTDataBlock dummyFFTBlock;

	public BaseFFTBearingAlgorithm(BearingProcess bearingProcess, 
			BearingAlgorithmParams algorithmParams, int groupIndex) {
		this.bearingProcess = bearingProcess;
		this.algorithmParams = algorithmParams;
		this.groupIndex = groupIndex;
		this.bearingLocaliserControl = bearingProcess.getBearingLocaliserControl();
		
		fftDataOrganiser = new FFTDataOrganiser(bearingProcess.getBearingLocaliserControl());

//		collatedFFTData = new ArrayList<>();
		
		prepareInput();
	}

	private void prepareInput() {
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		fftOrRawDataBlock = PamController.getInstance().getDataBlockByLongName(params.getDataSource());
		
		if (fftOrRawDataBlock == null) {
			System.out.println("No input data block to bearing localiser algorithm");
			inputType = null;
			return;
		}
		if (fftOrRawDataBlock instanceof OrganisedFFTData) {
			// see if there is a bespoke FFT data organiser. 
			fftDataOrganiser = ((OrganisedFFTData) fftOrRawDataBlock).getFFTDataOrganiser();
		}
		if (fftDataOrganiser == null) {
			fftDataOrganiser = new FFTDataOrganiser(bearingProcess.getBearingLocaliserControl());
		}
		
		inputType = fftDataOrganiser.suggestInputType(fftOrRawDataBlock);
		if (inputType == null) {
			System.out.printf("No campatible data for bearing localisation in %s\n", fftOrRawDataBlock.getLongDataName());
			return;
		}
		fftDataOrganiser.setInput(fftOrRawDataBlock, inputType);
		fftDataOrganiser.setFftLength(params.getFftLength());
		fftDataOrganiser.setFftHop(params.getFftHop());
		
		dummyFFTBlock = new FFTDataBlock("Dummy FFT Data", bearingProcess, fftOrRawDataBlock.getChannelMap(), params.getFftHop(), params.getFftLength());

	}


	/* (non-Javadoc)
	 * @see bearinglocaliser.algorithms.BearingAlgorithm#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object)
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		if (inputType == null) {
			return 0;
		}
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

	@Override
	public void setParams(BearingAlgorithmParams bearingAlgoParams) {
		// TODO Auto-generated method stub

	}

	@Override
	public BearingAlgorithmParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean prepare() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(PamDataUnit pamDataUnit, double sampleRate, BearingAlgorithmGroup bearingAlgoGroup) {
		BearingLocalisation bfLocalisation = null;
//		switch (inputType) {
//		case FFTData:
//			bfLocalisation = processFFTData(pamDataUnit, bearingAlgoGroup);
//			break;
//		case FFTDataHolder:
//			bfLocalisation = processFFTDataHolder(pamDataUnit, bearingAlgoGroup);
//			break;
//		case RAWDataHolder:
//			bfLocalisation = processRawDataHolder(pamDataUnit, bearingAlgoGroup);
//			break;
//		case RawData:
//			bfLocalisation = processRawData(pamDataUnit, bearingAlgoGroup);
//			break;
//		default:
//			return false;		
//		}
		FFTDataList fftDataList = null;
		try {
			 fftDataList = fftDataOrganiser.createFFTDataList(pamDataUnit, sampleRate, bearingAlgoGroup.channelMap);
		} catch (FFTDataException e) {
			System.out.println("Bearing localiser Exception " + e.getMessage());
			return false;
		}
		if (fftDataList == null || fftDataList.getFftDataUnits().size() == 0) {
			System.out.println("Bearing localiser error - no FFT data created ");
//			 try {
//				fftDataList = fftDataOrganiser.createFFTDataList(pamDataUnit, sampleRate, bearingAlgoGroup.channelMap);
//			} catch (FFTDataException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return false;
		}
		ArrayList<FFTDataUnit> fftList = fftDataList.getFftDataUnits();
		for (FFTDataUnit f:fftList) {
			f.setParentDataBlock(dummyFFTBlock);
		}
		bfLocalisation = processFFTData(pamDataUnit, bearingAlgoGroup, fftDataList);
//		System.out.println("BEARING: Bearing Annotation!: + " + Math.toDegrees(bfLocalisation.getAngles()[0])); 
		if (bfLocalisation != null) {
			pamDataUnit.addDataAnnotation(new BearingAnnotation(bearingLocaliserControl.getBearingAnnotationType(), bfLocalisation));
		}
		return (bfLocalisation != null);
	}
//
//	/**
//	 * Beam form by taking FFT data from an FFT data source and 
//	 * beam forming it. 
//	 * @param pamDataUnit trigger data unit
//	 * @return 
//	 */
//	private BearingLocalisation processFFTData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup) {
//		/**
//		 * Function works by getting an iterator from the source datablock, then pushing those data
//		 * into the beam form algorithms exactly as they would have been in real time. An observer monitors 
//		 * the beamogram output block (which is now invisible to the rest of PAMGuard) and collates
//		 * all the beamogram outputs into an array list. Appropriate information can then be extracted
//		 * from that summary list, displays filled, etc.  
//		 */
//		long t1 = pamDataUnit.getTimeMilliseconds();
//		long t2 = pamDataUnit.getEndTimeInMilliseconds();
//		double[] freqRange = pamDataUnit.getFrequency();
//		if (freqRange == null) {
//			return null;
//		}
//		//		System.out.printf("Beam Form from %s to %s, between %s\n", PamCalendar.formatTime(pamDataUnit.getTimeMilliseconds(), true), 
//		//				PamCalendar.formatTime(t2, true), FrequencyFormat.formatFrequencyRange(freqRange, true));
//
//		/*
//		 * Are going to need to be a bit cleverer here since when firing off the click detector, the
//		 * FFT data may not be there ! So need to queue up the new box and act only when the data have actually arrived. 
//		 */
//
//		collatedFFTData.clear();
//
//
////		int[] channelGroupLut = getChannelGroupLUT();
////		if (channelGroupLut == null) {
////			return null;
////		}
//		/*
//		 *  need to iterate through FFT data for the given time intervals and send them 
//		 *  off to the process... 
//		 */
//		if (fftSourceData == null) {
//			return null;
//		}
//		int groupChannelMap = beamGroup.channelMap;
//		int firstChannelMap = 1<<PamUtils.getLowestChannel(groupChannelMap);
//		synchronized (fftSourceData) {
//			ListIterator<FFTDataUnit> fftIterator = fftSourceData.getListIterator((long) t1, firstChannelMap, PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
//			if (fftIterator == null) {
//				System.out.println("FFT iterator unavailable ");
//				return null;
//			}
//			FFTDataUnit firstUnit = null, lastUnit = null;
//			boolean goodtoUse = false; // need to check we really are starting on the first channel or all will go wrong. 
//			while (fftIterator.hasNext()) {
//				FFTDataUnit fftData = fftIterator.next();
//				if ((groupChannelMap & fftData.getChannelBitmap()) == 0) {
//					continue;
//				}
//				if (fftData.getTimeMilliseconds() > t2) {
//					break;
//				}
//				if (fftData.getChannelBitmap() == firstChannelMap) {
//					goodtoUse = true;
//				}
//				if (goodtoUse == false) continue;
//				if (firstUnit == null) firstUnit = fftData;
//				lastUnit = fftData;
//				collatedFFTData.add(fftData);
//				/**
//				 * Send the FFT data into the beam former. 
//				 */
//				//				System.out.printf("FFT Data sample %d time %s channel 0x%X\n", fftData.getStartSample(), 
//				//						PamCalendar.formatTime(fftData.getTimeMilliseconds(), true), fftData.getChannelBitmap());
//			}
//			if (firstUnit == null) {
//				return null;
//			}
//			//			if (firstUnit != null) {
//			//				System.out.printf("Loaded %d datas from %s channel 0x%X to %s channel 0x%X\n", collatedFFTData.size(),
//			//						PamCalendar.formatTime(firstUnit.getTimeMilliseconds(), true), firstUnit.getChannelBitmap(),
//			//						PamCalendar.formatTime(lastUnit.getTimeMilliseconds(), true), lastUnit.getChannelBitmap());
//			//			}
//		}
//		return processFFTData(pamDataUnit, beamGroup, collatedFFTData);
//	}
//
//	/**
//	 * Beam form raw data extracted back from an original raw data 
//	 * audio source
//	 * @param pamDataUnit
//	 * @return
//	 */
//	private BearingLocalisation processRawData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup) {
//		long t1 = pamDataUnit.getTimeMilliseconds();
//		long t2 = pamDataUnit.getEndTimeInMilliseconds();
//		if (dummyRawDataBlock == null) {
//			System.err.println("Not raw data input in beam fomer: ");
//			return null;
//		}
//		double[][] rawData = null;
//		try {
//			rawData = dummyRawDataBlock.getSamplesForMillis(t1, t2-t1, beamGroup.channelMap);
//		} catch (RawDataUnavailableException e) {
//			System.err.println("Not raw data available in beam fomer: " + e.getMessage());
//			return null;
//		}
//		return processRawData(pamDataUnit, beamGroup, rawData, beamGroup.channelMap);
//	}
//
//	private BearingLocalisation processFFTDataHolder(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup) {
//		if (pamDataUnit instanceof FFTDataHolder) {
//			FFTDataHolder fftDataHolder = (FFTDataHolder) pamDataUnit;
//			List<FFTDataUnit> fftDatas = fftDataHolder.getFFTDataUnits(fftSourceData.getFftLength());
//			if (fftDatas != null && fftDatas.size() >= PamUtils.getNumChannels(pamDataUnit.getChannelBitmap())) {
//				return processFFTData(pamDataUnit, beamGroup, fftDatas);
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Beam form clips of raw data held within a data unit. 
//	 * @param pamDataUnit
//	 * @return
//	 */
//	private BearingLocalisation processRawDataHolder(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup) {
//		if (pamDataUnit instanceof RawDataHolder) {
//			RawDataHolder rawDataHolder = (RawDataHolder) pamDataUnit;
//			double[][] wavData = rawDataHolder.getWaveData();
//			if (wavData != null && wavData.length == PamUtils.getNumChannels(pamDataUnit.getChannelBitmap())) {
//				return processRawData(pamDataUnit, beamGroup, wavData, pamDataUnit.getChannelBitmap());
//			}
//		}
//		return null;
//	}
//
//	private BearingLocalisation processRawData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup, double[][] wavData, int channelMap) {
//		int fftLength = fftSourceData.getFftLength();
//		int fftHop = fftSourceData.getFftHop();
//		int nWavChan = wavData.length;
//		int nMapChan = PamUtils.getNumChannels(channelMap);
//		if (nWavChan != nMapChan) {
//			System.err.printf("Beam former channel number mismatch: %d in wave and %d in channel map for %s\n", 
//					nWavChan, nMapChan, pamDataUnit);
//			return null;
//		}
//		if (nWavChan == 0) {
//			return null;
//		}
//		int nSamples = wavData[0].length;
//		/*
//		 * Check the length of the raw data, rounding it up to the next hop unless it's already >>
//		 * several bins.  
//		 */
//		double dFFT = (nSamples - fftLength) / fftHop + 1;
//		if (dFFT < 4) { // consider padding the data up to a round number of FFT's. 
//			int nFFT = Math.max((int) Math.ceil(dFFT), 1);
//			int wantedSamp = (nFFT-1) * fftHop + fftLength;
//			if (wantedSamp > nSamples) {
//				// pad the data, copying the entire array (avoids modifying internal data in the click detector). 
//				wavData = Arrays.copyOf(wavData, wavData.length);
//				for (int i = 0; i < wavData.length; i++) {
//					wavData[i] = Arrays.copyOf(wavData[i], wantedSamp);
//				}
//			}
//		}
//		// find the FFT process
//		PamFFTProcess fftProcess = (PamFFTProcess) privateFFTControl.getPamProcess(0);
//		fftProcess.clearTempStores();
//		collatedFFTData.clear();
//		/*
//		 * Now create raw data units to send to the fft module, everything should
//		 * then happen quite automatically. 
//		 */
//		for (int i = 0; i < nWavChan; i++) {
//			int iChan = PamUtils.getNthChannel(i, channelMap);
//			RawDataUnit rawDataUnit = new RawDataUnit(pamDataUnit.getTimeMilliseconds(), 1<<iChan, 0, wavData[i].length);
//			rawDataUnit.setRawData(wavData[i], false);
//			fftProcess.update(null, rawDataUnit);
//		}
//		return processFFTData(pamDataUnit, beamGroup, collatedFFTData);
//	}
	/**
	 * Convert a frequency to the nearest bin. The range of the bin
	 * will be from 0 to fftLength()/2 inclusive, so when looping to a higher 
	 * limit, loop to < the top bin number!
	 * @param frequency Frequency in Hz
	 * @return FFT bin number. 
	 */
	public int frequencyToBin(double frequency) {
		if (fftOrRawDataBlock == null) {
				return 0;
		}
		int fftLength = bearingLocaliserControl.getBearingLocaliserParams().getFftLength();
		double binSize = fftOrRawDataBlock.getSampleRate() / fftLength;
		int bin = (int) Math.round(frequency/binSize);
		return Math.max(0, Math.min(bin, fftLength/2));
	}

	/**
	 * Convert an array of frequency values to the nearest bins. The range of the bins
	 * will be from 0 to fftLength()/2 inclusive, so when looping to a higher 
	 * limit, loop to < the top bin number!
	 * @param frequency Frequencies in Hz
	 * @return FFT bin numbers. 
	 */
	public int[] frequencyToBin(double[] frequency) {
		if (frequency == null) {
			return null;
		}
		int[] bins = new int[frequency.length];
		for (int i = 0; i < frequency.length; i++) {
			bins[i] = frequencyToBin(frequency[i]);
		}
		return bins;
	}
	
	
	/**
	 * this is where we end up when any raw or FFT data has been extracted and, where necessary
	 * FFT'd so we've an array of FFT data over all channels in the group. 
	 * @param pamDataUnit
	 * @param beamGroup
	 * @param fftDataList
	 * @return a valid bearing loclaisation or null if it can't be computed 
	 */
	abstract public BearingLocalisation processFFTData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup, FFTDataList fftDataList);

	/**
	 * @return the fftSourceData
	 */
	public FFTDataBlock getFftSourceData() {
		return dummyFFTBlock;
	}

	/**
	 * @return the algorithmParams
	 */
	public BearingAlgorithmParams getAlgorithmParams() {
		return algorithmParams;
	}

	public void setAlgorithmParams(BearingAlgorithmParams algorithmParams) {
		this.algorithmParams = algorithmParams;
	}
}
