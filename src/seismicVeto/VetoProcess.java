package seismicVeto;

import java.awt.Color;
import java.util.Random;

import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;

public class VetoProcess extends PamProcess {

	VetoController vetoController;

	PamRawDataBlock rawOutputData, rawInputData;

	FFTDataBlock fftOutputData, fftInputData;

	PamDataBlock<VetoDataUnit> vetoOutputData;

	/*
	 * data block used exclusively for parsing data on SNR levels to 
	 * plug in display panels 
	 */
	PamDataBlock<VetoBackgroundDataUnit> backgroundDataBlock;


	// parameters used in the actual veto. 
	ChannelDetector[] channelDetectors;
	private int bin1, bin2;
	private double thresholdRatio;
	private double backgroundUpdateConstant1, backgroundUpdateConstant;
	AcquisitionProcess daqProcess;

	// veto flags and numbers (held separately for each channel)
	private long[] vetoStartTime = new long[PamConstants.MAX_CHANNELS];
	private long[] vetoEndTime = new long[PamConstants.MAX_CHANNELS];
	private long vetoPreSamples, vetoPostSamples;

	private FastFFT fastFFT = new FastFFT();

	private static final int NRANDOMSAMPLES = 20;
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 10, 10, true, Color.GREEN, Color.RED);

	Random r = new Random();

	public VetoProcess(VetoController vetoController) {

		super(vetoController, null);

		this.vetoController = vetoController;

		addOutputDataBlock(rawOutputData = new PamRawDataBlock("Vetoed Audio data", this, 0, 1));
		addOutputDataBlock(fftOutputData = new FFTDataBlock("Vetoed FFT data", this, 0, 0, 0));
		fftOutputData.setRecycle(true);

		backgroundDataBlock = new PamDataBlock<VetoBackgroundDataUnit>(VetoBackgroundDataUnit.class, 
				vetoController.getUnitName(), this, 0);


		vetoOutputData = new PamDataBlock<VetoDataUnit>(VetoDataUnit.class, 
				vetoController.getUnitName(), this, 0);
		PamDetectionOverlayGraphics detectionOverlayGraphics= new PamDetectionOverlayGraphics(vetoOutputData, 
				new PamSymbol(defSymbol));
		vetoOutputData.setOverlayDraw(detectionOverlayGraphics);
		vetoOutputData.setPamSymbolManager(new StandardSymbolManager(vetoOutputData, defSymbol, true));
		vetoOutputData.SetLogging(new VetoLogging(vetoOutputData, vetoController));
		addOutputDataBlock(vetoOutputData);
		
		r.setSeed(System.currentTimeMillis());
	}

	int outputCount;
	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		outputCount = 0;
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		useNewParams();
	}

	@Override
	public void noteNewSettings() {
		// TODO Auto-generated method stub
		super.noteNewSettings();
		useNewParams();
	}

	public void useNewParams() {
		
		fftInputData = (FFTDataBlock) PamController.getInstance().getFFTDataBlock(vetoController.vetoParameters.dataSourceName);
		setParentDataBlock(fftInputData);
		fftOutputData.setChannelMap(vetoController.vetoParameters.channelBitmap);
		rawOutputData.setChannelMap(vetoController.vetoParameters.channelBitmap);
		backgroundDataBlock.setChannelMap(vetoController.vetoParameters.channelBitmap);

		if (fftInputData == null) {
			return;
		}
		fftOutputData.setFftHop(fftInputData.getFftHop());
		fftOutputData.setFftLength(fftInputData.getFftLength());
		// also need to find the raw data feeding the fftInputProcess
		PamProcess aProcess = fftInputData.getParentProcess();
		PamDataBlock aDataBlock;
		rawInputData = null;
		int nD;
		while (aProcess != null) {
			aDataBlock = aProcess.getParentDataBlock();
			if (aDataBlock == null) {
				break;
			}
			if (aDataBlock.getUnitClass() == RawDataUnit.class) {
				rawInputData = (PamRawDataBlock) aDataBlock;
				break;
			}
			aProcess = aDataBlock.getParentProcess();
		}
		if (rawInputData != null) {
			rawInputData.addObserver(this, true);
		}

		/*
		 * This should always go back to an Acquisition process by working
		 * back along the chain of data blocks and pamprocesses.
		 * Surround it in a try/catch block, just in case something happened
		 * and we don't actually return an AcquisitionProcess object 
		 */
		try {
			daqProcess = (AcquisitionProcess) getSourceProcess();
		}
		catch (ClassCastException cce) {
//			daqProcess=null;
		}

//		parentFFTSource = (FFTDataSource) fftInputData.getParentProcess();

		vetoPreSamples = (long) (getSampleRate() * vetoController.vetoParameters.vetoPreTime);
		vetoPostSamples = (long) (getSampleRate() * vetoController.vetoParameters.vetoPostTime);

		/*
		 * usedChannels will be a combination of what we want and what's available.
		 */
		int usedChannels = fftInputData.getChannelMap() & vetoController.vetoParameters.channelBitmap;
		/**
		 * allocate references to a list of detectors - one for each channel used. 
		 */
		channelDetectors = new ChannelDetector[PamUtils.getHighestChannel(usedChannels)+1];
		for (int i = 0; i <= PamUtils.getHighestChannel(usedChannels); i++) {
			if (((1<<i) & usedChannels) > 0) {
				channelDetectors[i] = new ChannelDetector(i);
			}
		}

		// get integer bins for the energy sum.
		bin1 = (int) (fftInputData.getFftLength() / fftInputData.getSampleRate() * 
				vetoController.vetoParameters.f1);
		bin2 = (int) (fftInputData.getFftLength() / fftInputData.getSampleRate() * 
				vetoController.vetoParameters.f2);
		bin1 = Math.min(bin1, fftInputData.getFftLength()/2-1);
		bin1 = Math.max(bin1, 0);
		bin2 = Math.min(bin2, fftInputData.getFftLength()/2-1);
		bin2 = Math.max(bin2, 0);

		/* 
		 * work out decay constants for background update - this will be a decaying average over time.
		 * 
		 */
		double secsPerBin = fftInputData.getFftHop() / fftInputData.getSampleRate();
		backgroundUpdateConstant = secsPerBin / vetoController.vetoParameters.backgroundConstant;
		backgroundUpdateConstant1 = 1.0 - backgroundUpdateConstant;

		/*
		 * convert the threshold which was set in dB to a simple energy ratio
		 */
		thresholdRatio = Math.pow(10., vetoController.vetoParameters.threshold/10.);
	}

	private void clearVetos() {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			clearVeto(i);
		}
	}

	private void clearVeto(int channel, long sampleTime) {
		if (vetoEndTime[channel] < sampleTime) {
			clearVeto(channel);
		}
	}
	private void clearVeto(long sampleTime) {
		// if the end time for the veto on a channel is less than the sampleTime
		// then clear the veto on that channel.
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((vetoController.vetoParameters.channelBitmap & 1<<i) == 0) {
				continue;
			}
			if (vetoEndTime[i] < sampleTime) {
				clearVeto(i);
			}
		}
	}

	private boolean isVetoed(int channel, long sampleNumber) {
		return (sampleNumber < vetoEndTime[channel] && sampleNumber > vetoStartTime[channel]);
	}

	private void clearVeto(int channel) {
		vetoStartTime[channel] = vetoEndTime[channel] = 0;
	}

	private void setVetoTimes(int channel, long signalTime) {
		if (vetoStartTime[channel] != 0) {
			vetoStartTime[channel] = signalTime - vetoPreSamples;
		}
		vetoEndTime[channel] = signalTime + vetoPostSamples;
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		long t =  (long) (vetoController.vetoParameters.vetoPreTime * 1000.) + 500;
		if (fftInputData != null) { // get an extra 2 fft blocks. 
			t += (fftInputData.getFftLength() * 2000) / getSampleRate();
		}
		return t;
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == this.fftInputData) {
			newFFTData(o, arg);
		}
		else if (o == this.rawInputData) {
			newRawData(o, arg);
		}
	}

	private void newRawData(PamObservable o, PamDataUnit arg) {
		// work out how many data blocs back we need to go
		// and pass on the right one (basically create a delay)

		RawDataUnit rawDataUnit = (RawDataUnit) arg; 
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		long searchSample = rawDataUnit.getStartSample() - vetoPreSamples;
		if (searchSample < 0) {
			return;
		}
		int searchChannels =rawDataUnit.getChannelBitmap();
		// search backwards through the data and take the first one we come to which 
		// has a start sample EARLIER or EQUAL to searchSample
		rawDataUnit = rawInputData.getPreceedingUnit(absSamplesToMilliseconds(searchSample), searchChannels);
//		while (rawDataUnit != null) {
//			if (rawDataUnit.getChannelBitmap() == searchChannels && 
//					rawDataUnit.getStartSample() <= searchSample) {
//				break;
//			}
//			rawDataUnit = rawInputData.getDataUnit(rawDataUnit.getAbsBlockIndex()-1, PamDataBlock.REFERENCE_ABSOLUTE);
//		}
		if (rawDataUnit == null) {
			System.out.println(String.format("Channel %d No Raw data from unit at sample %d", 
					searchChannels, ((RawDataUnit) arg).getStartSample()));
//			System.out.println("No raw data from unit at sample " + ((RawDataUnit) arg).getStartSample());
			rawDataUnit = rawInputData.getPreceedingUnit(absSamplesToMilliseconds(searchSample), searchChannels);
			return;
		}
		// need to clone the raw data unit, and set the data according to the veto status
		RawDataUnit newUnit = rawOutputData.getRecycledUnit();
		if (newUnit == null) {
			newUnit = new RawDataUnit(rawDataUnit.getTimeMilliseconds(), rawDataUnit.getChannelBitmap(), 
					rawDataUnit.getStartSample(), rawDataUnit.getSampleDuration());
		}
		else {
			newUnit.setInfo(rawDataUnit.getTimeMilliseconds(), rawDataUnit.getChannelBitmap(), 
					rawDataUnit.getStartSample(), rawDataUnit.getSampleDuration());
		}
		double[] newData= newUnit.getRawData(); // should exist if it wasn't recycled.
		if (newData == null || newData.length != rawDataUnit.getSampleDuration()) {
			newData = new double[rawDataUnit.getSampleDuration().intValue()]; // a little awkward since duration is actually stored as Long
		}
		if (isVetoed(chan, rawDataUnit.getStartSample())) {
			if (vetoController.vetoParameters.randomFillWaveform) {
				channelDetectors[chan].fillWaveData(newData);
			}
			else {
				channelDetectors[chan].clearWaveData(newData);
			}
		}
		else {
			double[] oldData = rawDataUnit.getRawData();
			for (int i = 0; i < oldData.length; i++) {
				newData[i] = oldData[i];
			}

		}

		newUnit.setRawData(newData, true);
		rawOutputData.addPamData(newUnit);
	}
	private void newFFTData(PamObservable o, PamDataUnit arg)
	{	
		// see which channel it's from
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		clearVeto(chan, fftDataUnit.getStartSample()); // clear if we're now beyond the veto time. 
		// check that a detector has been instantiated for that detector
		if (channelDetectors == null || 
				channelDetectors.length <= chan || 
				channelDetectors[chan] == null) {
//			System.out.println("No channel detector for channel " + chan);
			return;
		}

		channelDetectors[chan].newData(o, fftDataUnit);

		long searchSample = fftDataUnit.getStartSample() - vetoPreSamples;
		if (searchSample < 1000) {
			return;
		}
		int searchChannels = fftDataUnit.getChannelBitmap();
		// search backwards through the data and take the first one we come to which 
		// has a start sample EARLIER or EQUAL to searchSample
		fftDataUnit = fftInputData.getPreceedingUnit(absSamplesToMilliseconds(searchSample), searchChannels);
//		while (fftDataUnit != null) {
//			if (fftDataUnit.getChannelBitmap() == searchChannels && 
//					fftDataUnit.getStartSample() <= searchSample) {
//				break;
//			}
//			fftDataUnit = fftInputData.getDataUnit(fftDataUnit.getAbsBlockIndex()-1, PamDataBlock.REFERENCE_ABSOLUTE);
//		}
		if (fftDataUnit == null) {
			System.out.println(String.format("Channel %d No fft data from unit at sample %d, time %d", 
					searchChannels, ((FFTDataUnit) arg).getStartSample(), absSamplesToMilliseconds(searchSample)));
			fftInputData.dumpBlockContents();
			fftDataUnit = fftInputData.getPreceedingUnit(absSamplesToMilliseconds(searchSample), searchChannels);
			return;
		}
		FFTDataUnit newFFTUnit = fftOutputData.getRecycledUnit();		

		if (newFFTUnit == null) {
			newFFTUnit = new FFTDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
					fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), null, fftDataUnit.getFftSlice());
		}
		else {
			newFFTUnit.setInfo(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
					fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), fftDataUnit.getFftSlice());
		}


		ComplexArray oldData = fftDataUnit.getFftData();

		ComplexArray newData = newFFTUnit.getFftData();
		if (newData == null) {
			newData = new ComplexArray(oldData.length());//fftOutputData.getComplexArray(oldData.length());
		}

		if (isVetoed(chan, fftDataUnit.getStartSample())) {
			// leave it filled with zeros
			if (vetoController.vetoParameters.randomFillSpectorgram) {
				double[] referenceData = channelDetectors[chan].getRunningBackground();
				for (int i = 0; i < oldData.length(); i++) {
					newData.set(i, Math.sqrt(referenceData[i]), 0);
				}
			}
			else {
				for (int i = 0; i < oldData.length(); i++) {
					newData.set(i, 0, 0);
				}
			}
		}
		else {
			newData = oldData.clone();
		}
		newFFTUnit.setFftData(newData);
		fftOutputData.addPamData(newFFTUnit);
//		if (outputCount++ < 10) {
//			System.out.println("*****************************************");
//			System.out.println("Added new data to fft output stream");
//			System.out.println(String.format("Chan %d, Sample %d, ms %d", newFFTUnit.getChannelBitmap(),
//					newFFTUnit.getStartSample(), newFFTUnit.getTimeMilliseconds()));
//			fftDataUnit = (FFTDataUnit) arg;
//			System.out.println(String.format("Chan %d, Sample %d, ms %d", fftDataUnit.getChannelBitmap(),
//					fftDataUnit.getStartSample(), fftDataUnit.getTimeMilliseconds()));
//		}
	}

	public int getFftHop() {
		if (fftInputData == null) {
			return 0;
		}
		return fftInputData.getFftHop();
	}

	public int getFftLength() {
		if (fftInputData == null) {
			return 0;
		}
		return fftInputData.getFftLength();
	}

	
	public FFTDataBlock getFftInputData() {
		return fftInputData;
	}

	public FFTDataBlock getFftOutputData() {
		return fftOutputData;
	}

	public PamRawDataBlock getRawInputData() {
		return rawInputData;
	}

	public PamRawDataBlock getRawOutputData() {
		return rawOutputData;
	}

	/**
	 * Since the detector may be running on several channels, make a sub class for the actual detector
	 * code so that multiple instances may be created. 
	 * @author Doug Gillespie
	 *
	 */
	class ChannelDetector {

		/*
		 * Which channel is this detector operating on
		 */
		private int channel;

		/* 
		 * currently above threshold or not ? 
		 */
		private boolean detectionOn = false;

		/*
		 * measure of background noise
		 */
		private double background = 0;

		/*
		 * how many times have new data arrived. 
		 */
		private int callCount = 0;

		private double[] runningBackground;

		/*
		 * use the first setupCount datas to set the background
		 * before starting to do any detection. 
		 */
		static final int setupCount = 20;
		
		/**
		 * Random waveofrms for use in simulated wave data.
		 */
		private double[][] randomWaveData;
		
		// counters for random waves. 
		private int selectedWave1, selectedWave2, randomSample;

		/*
		 * some information about each detection.
		 */
//		long detectionStart;
		private long detectionEndSample;
		private long detectionStartSample; 
		private double detectionEnergy;
		private int detectionSliceCount; 

		public ChannelDetector(int channel) {
			this.channel = channel;
		}

		public void pamStart() {
			background = 0;
			detectionOn = false; 
			callCount = 0;
		}

		/**
		 * Performs the same function as newData in the outer class, but this
		 * time it should only ever get called with data for a single channel.
		 * For the first few calls, it just updates the background by taking a straight mean
		 * of the energy values. After that it calculates a decaying average of 
		 * the background.
		 * @param o
		 * @param arg
		 */
		public void newData(PamObservable o, FFTDataUnit arg) {

			boolean overThresh = false;
			
			double energy = energySum(arg.getFftData(), bin1, bin2);
			
			if (callCount < setupCount) {
//				background += energy / setupCount;
//				return;
			}
			else {
				overThresh = (energy > background * thresholdRatio);
			}

			if (runningBackground == null || runningBackground.length != arg.getFftData().length()) {
				runningBackground = new double[arg.getFftData().length()];
				callCount = 0;
			}

			if (overThresh) {
				setVetoTimes(channel, arg.getStartSample());
			}
			/*
			 * Need to put the dB over background measures into a datablock so that the 
			 * spectrogram display plug ins can subscribe to it and plot it. 
			 */
			double lastDbValue = 10 * Math.log10(energy / background);
			VetoBackgroundDataUnit bdu = new VetoBackgroundDataUnit(absSamplesToMilliseconds(arg.getStartSample()),
					arg.getChannelBitmap(), arg.getStartSample(), 0, lastDbValue, isVetoed(channel, arg.getStartSample()));
			backgroundDataBlock.addPamData(bdu);

			// reduce background updating according to how far over threshold the signal is. 
			double b1 = backgroundUpdateConstant, b2 = backgroundUpdateConstant1;
			if (lastDbValue > vetoController.vetoParameters.threshold / 10) {
				b1 /= (energy / background);
//				b1 = 0;
				b2 = 1.0 - b1;
			}

			/*
			 * During startup, just add up the mean of the first setupCount entries. 
			 * otherwise reduce the values of the background measures according to how much over
			 * threshold it is. 
			 */
			if (callCount < setupCount) {
				b2 = 1;
				b1 = 1.0/setupCount;
				callCount++;
			}
			background *= b2;
			background += (energy * b1);
			if (!isVetoed(channel, arg.getStartSample())) {
				ComplexArray fftData = arg.getFftData();

				for (int i = 0; i < runningBackground.length; i++) {
					runningBackground[i] *= b2;
					runningBackground[i] += fftData.magsq(i) * b1;
				}
			}


//			background *= backgroundUpdateConstant1;
//			background += (energy * backgroundUpdateConstant);

			if (overThresh && !detectionOn) {
				startDetection(arg, energy);
				if (vetoController.vetoParameters.randomFillWaveform) {
					randomWaveData = prepareRandomWaveData(NRANDOMSAMPLES);
				}
			}
			else if (overThresh && detectionOn) {
				continueDetection(arg, energy);
			}
			else if (!overThresh && detectionOn) {
				endDetection();
			}
			// don't need to handle overThresh == false && detectionOn == false

		}
		protected void fillWaveData(double[] data) {
			if (randomWaveData == null){
				System.out.println("Random wave data is unavailable");
				clearWaveData(data);
				return;
			}
			double[] randomData1 = randomWaveData[selectedWave1];
			double[] randomData2 = randomWaveData[selectedWave2];
			if (randomData1 == null || randomData2 == null) return;
			int sampleLength = randomData1.length; 
//			int iD = 0;
//			System.out.println("iD = " + iD + "; randomSample = " + randomSample + " vaves = " + selectedWave1 + " and " + selectedWave2 + 
//					" Data length = " + data.length + "; Sample Length " + sampleLength);
			for (int iD = 0; iD < data.length; iD++) {
				data[iD] = randomData1[randomSample] + randomData2[randomSample + sampleLength/2];
				randomSample++;				
				// shuffle waves along, so that we're always taking from two of them
				// selecting a new one at random from the list. 
				if (randomSample == sampleLength/2) {
					selectedWave2 = selectedWave1;
					randomData2 = randomData1;
					selectedWave1 = (int) Math.floor(r.nextDouble() * NRANDOMSAMPLES);
					randomData1 = randomWaveData[selectedWave1];
					randomSample = 0;
				}
				if (iD == data.length) {
					return;
				}					
			}
//				while (randomSample < sampleLength / 2) {
//					data[iD++] = randomData1[randomSample] + randomData2[randomSample + sampleLength/2];
//					randomSample++;				
//					// shuffle waves along, so that we're always taking from two of them
//					// selecting a new one at random from the list. 
//					if (randomSample == sampleLength/2) {
//						selectedWave2 = selectedWave1;
//						randomData2 = randomData1;
//						selectedWave1 = (int) Math.floor(r.nextDouble() * NRANDOMSAMPLES);
//						randomData1 = randomWaveData[selectedWave1];
//						break;
//					}
//					if (iD == data.length) {
//						return;
//					}
//				}
//				if (iD == data.length) {
//					return;
//				}
//				while (randomSample < sampleLength) {
////					if (iD == 1023) {
////					System.out.println("iD = " + iD + "; randomSample = " + randomSample + " vaves = " + selectedWave1 + " and " + selectedWave2 + 
////							" Data length = " + data.length + "; Sample Length " + sampleLength);
////					}
//					data[iD++] = randomData2[randomSample] + randomData1[randomSample - sampleLength/2];
//					randomSample++;
//					if (randomSample == sampleLength) {
//						selectedWave2 = selectedWave1;
//						randomData2 = randomData1;
//						selectedWave1 = (int) Math.floor(r.nextDouble() * NRANDOMSAMPLES);
//						randomData1 = randomWaveData[selectedWave1];
//						randomSample = 0;
//						break;
//					}
//					if (iD == data.length) {
//						return;
//					}
//				}
//				if (iD == data.length) {
//					return;
//				}
//			}
		}
		protected void clearWaveData(double[] data) {
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
		}
		/**
		 * generate a number of random samples of data which follow a particular
		 * spectral shape. These will be stitched together in random order to make a 
		 * longer wave sample in the veto
		 * @param nSamples
		 * @param sampleLength
		 */
		private double[][] prepareRandomWaveData(int nSamples) {
			selectedWave1 = 0;
			selectedWave2 = 1;
			randomSample = 0;
//			System.out.println("Prepare random wave samples for channel " + channel);
			double[] refBackground = runningBackground;
			if (refBackground == null) return null;
			int sampleLength = refBackground.length * 2;
			double[][] samples = new double[nSamples][sampleLength];
			double[] sample;
			double[] window = new double[sampleLength];
			double[] specShape = new double[sampleLength];
			double scaleFactor = sampleLength * Math.sqrt(sampleLength);
//			double scaleFactor = Math.sqrt(sampleLength);
			for (int i = 0; i < sampleLength/2; i++) {
				window[i] = window[sampleLength-1-i] = Math.sqrt(2.*i/sampleLength);
				specShape[i] = specShape[sampleLength-1-i] = Math.sqrt(refBackground[i]);
			}
			Complex[] fastFftData = null;
			Complex[] fftData = new Complex[sampleLength];
			int logFFTLen = PamUtils.log2(sampleLength);
			for (int iSample = 0; iSample < nSamples; iSample++) {
				sample = samples[iSample];
				for (int i = 0; i < sampleLength; i++) {
					sample[i] = r.nextGaussian() * window[i];
				}
				fastFftData = fastFFT.rfft(sample, fastFftData, logFFTLen);
				// fftData is only half the lenght (form the fast fft)
				for (int i = 0; i < sampleLength/2; i++) {
					fftData[i] = fastFftData[i].times(specShape[i]);
					fftData[sampleLength-1-i] = fftData[i].conj();
				}
				fastFFT.ifft(fftData, logFFTLen);
				for (int i = 0; i < sampleLength; i++) {
					sample[i] = fftData[i].real / scaleFactor;
				}
			}
			return samples;
		}
		

		private double energySum(ComplexArray complexArray, int bin1, int bin2) {
			double e = 0;
			for (int i = bin1; i <= bin2; i++) {
				e += complexArray.magsq(i);
			}
			return e;
		}

		private void startDetection(FFTDataUnit dataUnit, double energy) {
			detectionStartSample = dataUnit.getStartSample();
			detectionEndSample = detectionStartSample + dataUnit.getSampleDuration();
			detectionEnergy = energy;
			detectionSliceCount = 1;
			detectionOn = true;
		}
		private void continueDetection(FFTDataUnit dataUnit, double energy) {
			detectionEndSample = dataUnit.getStartSample() + dataUnit.getSampleDuration();
			detectionEnergy += energy;
			detectionSliceCount += 1;
		}
		private void endDetection() {
			/*
			 * Get a new data unit from the data block (allows efficient recycling of 
			 * unused units)
			 */
//			PamDataUnit newDataunit = outputDataBlock.getNewUnit(detectionStartSample, 
//			detectionEndSample-detectionStartSample, 1<<channel);
			VetoDataUnit wdu  = new VetoDataUnit(absSamplesToMilliseconds(detectionStartSample),
					1<<channel, detectionStartSample, detectionEndSample-detectionStartSample);

			/*
			 * fill in detection information 
			 */
			wdu.setFrequency(new double[]{vetoController.vetoParameters.f1,
					vetoController.vetoParameters.f2});
//			newDataunit.frequency[0] = workshopController.workshopProcessParameters.lowFreq;
//			newDataunit.frequency[1] = workshopController.workshopProcessParameters.highFreq;
			/*
			 * now work out the energy in dB re 1 micropascal. This requires knowledge from
			 * both the hydrophone array and from the digitiser. Fortunately, the Acquisitionprocess, which 
			 * is a subclass of PamProcess can handle all this for us. 
			 */
			double aveAmplitude = detectionEnergy / detectionSliceCount;
			wdu.setMeasuredAmpAndType(daqProcess.fftAmplitude2dB(aveAmplitude, 
					channel, fftInputData.getSampleRate(), 
					fftInputData.getFftLength(), true, false), DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA);

			/*
			 * put the unit back into the datablock, at which point all subscribers will
			 * be notified. 
			 */
			vetoOutputData.addPamData(wdu);

			detectionOn = false;
		}

		public double[] getRunningBackground() {
			return runningBackground;
		}

	}

}
