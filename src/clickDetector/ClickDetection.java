package clickDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.SnapshotGeometry;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamModel.SMRUEnable;
import PamUtils.CPUMonitor;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.ChannelListManager;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import clickDetector.ClickDetector.ChannelGroupDetector;
import clickDetector.ClickClassifiers.basic.BasicClickIdentifier;
import clickDetector.ClickClassifiers.basicSweep.ZeroCrossingStats;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import fftFilter.FFTFilterParams;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;
import pamMaths.PamVector;

/**
 * Class for Click Detector clicks. 
 * 
 * @author Doug Gillespie
 *
 */
@SuppressWarnings("rawtypes")
public class ClickDetection extends PamDataUnit<PamDataUnit, PamDataUnit> implements PamDetection, RawDataHolder, FFTDataHolder {

	//	private Click click;

	static public final int CLICK_CLICK = 0;
	static public final int CLICK_NOISEWAVE = 5; // compatibility with RC

	/*
	 * Bitwise flags to go into the clickFlags variable. 
	 */
	static public final int CLICK_FLAG_ECHO = 0x1;

	/**
	 * Click number in list. 
	 */
	long clickNumber;

	int triggerList;

	/**
	 * Wave data is the original double waveform from the
	 * sound source of the click detector. 
	 */
	private double[][] waveData;

	//private double[][] filteredWaveData;

	/**
	 * Compressed wave data is signed integer 8 bit data, scaled so that
	 * the maximum value is 127 or -127, this is related to 
	 * the original waveData by the scaling constant waveAmplitude
	 * where compressedWaveData = waveData * 127. / waveAmplitude;
	 */
	private byte[][] compressedWaveData;

	private double waveAmplitude;

	private double[] amplitude;

	boolean tracked;

	// these next three are mainly to do with saving in RC type files.
	int flags;

	/**
	 * Click data type
	 */
	byte dataType = CLICK_CLICK;

	protected long filePos;

	/**
	 * Click species. 
	 */
	private byte clickType; // click species. 

	private ClickDetector clickDetector;

	//	int eventId;

	boolean discard = false;

	private double ICI = -1; // filled in by click train id stuff.

	private double tempICI; // used when no event information available.  

	// some stuff used many times, so held internally to avoid repeats
	//private double[][] powerSpectra;

	//private double[] totalPowerSpectrum;

	//private ComplexArray[] complexSpectrum;

	private int currentSpectrumLength = 0;

	//private double[][] analyticWaveform;

	private int nChan;

	private int shortestFFTLength = 0;
	
	/**
	 * Handles acosutic data transfroms such as filtering waveforms, converting to spectra and 
	 * spectrograms. 
	 */
	private RawDataTransforms rawdataTransforms; 
	
	/**
	 * Spectrogram clip for  very long clicks. This is used by displays to
	 * prevent recalculation of muliple FFT. 
	 */
	private ClickSpectrogram clickSpectrogram; 
	

	public int getShortestFFTLength() {
		if (shortestFFTLength > 0) {
			return shortestFFTLength;
		}
		shortestFFTLength = PamUtils.getMinFftLength(getSampleDuration());
		return shortestFFTLength;                
	}

	private ClickLocalisation clickLocalisation;

	/**
	 * Number of time delays.  Typically nChan*(nChan-1)/2.
	 */
	private int nDelays;

	/**
	 * double array of Time Delays measured in samples.  Note that
	 * what is stored here is also converted to a double array of
	 * time delays <em>in seconds</em>, and stored in the DataUnitBaseData object
	 */
	private double[] delaysInSamples;
	private ChannelGroupDetector channelGroupDetector;

//	private ClickDetectionMatch clickDetectionMatch;

	/**
	 * save the zero crossing stats for Rocca to use during classification
	 * 2014/08/06 MO
	 */
	private ZeroCrossingStats[] zcs = null;

	/**
	 * In the case of Viewer Mode, save the Offline Event ID before sending to Rocca.
	 * Note that this is different than "eventID", which is the Click Train ID
	 */
	private int offlineEventID;


	/**
	 * click flags continas bitwise information about the click - such as whether it's an echo.
	 */
	private int clickFlags;

	//	public Click(ClickDetector clickDetector, long startSample, int nChan,
	//			long duration, int channelList, int triggerList) {
	public ClickDetection(int channelBitmap, long startSample, long duration, 
			ClickDetector clickDetector, ChannelGroupDetector channelGroupDetector, int triggerList) {

		super(clickDetector.absSamplesToMilliseconds(startSample), channelBitmap, startSample, duration);
		this.setChannelGroupDetector(channelGroupDetector);
		nChan = PamUtils.getNumChannels(channelBitmap);
		nDelays = nChan*(nChan-1)/2;
		delaysInSamples = new double[nDelays];
		this.setClickDetector(clickDetector);
		amplitude = new double[nChan];
		this.triggerList = triggerList;
		if (clickDetector != null && channelGroupDetector == null) {
			this.setChannelGroupDetector(clickDetector.findChannelGroupDetector(channelBitmap));
		}
		this.rawdataTransforms = new RawDataTransforms(this); 
		//		this.click = click;
	}


	public ClickDetection(ClickDetector clickDetector) {
		super(0, 0, 0, 0);
		this.setClickDetector(clickDetector);
		this.rawdataTransforms = new RawDataTransforms(this); 
	}

	//	public Click getClick() {
	//		return click;
	//	}
	//
	//	public void setClick(Click click) {
	//		this.click = click;
	//	}

	public boolean isTracked() {
		return tracked;
	}

	public void setTracked(boolean tracked) {
		this.tracked = tracked;
	}

	/**
	 * Get thd event id for a click. no longer store
	 * EventId within the click, but get it from the super detection. 
	 * @return Event id (same as database index of an event). 
	 */
	public int getEventId() {
		return super.getSuperId(OfflineEventDataUnit.class);
		//		return eventId;
	}

	//	public void setEventId(int eventId) {
	//		this.eventId = eventId;
	//	}

	/**
	 * 
	 * @return true if the echo flag is set. 
	 */
	public boolean isEcho() {
		return ((clickFlags & CLICK_FLAG_ECHO) == CLICK_FLAG_ECHO);
	}

	/**
	 * 
	 * @param isEcho set the click echo flag. 
	 */
	public void setEcho(boolean isEcho) {
		if (isEcho) {
			clickFlags |= CLICK_FLAG_ECHO;
		}
		else {
			clickFlags &= ~CLICK_FLAG_ECHO;
		}
	}

	/**
	 * Returns the complex spectrum for a given channel using the shortest
	 * possible FFT length
	 * 
	 * @param channel
	 * @return The complex spectrum
	 */
	public ComplexArray getComplexSpectrum(int channel) {
		return getComplexSpectrum(channel, PamUtils.getMinFftLength(getSampleDuration()));
	}

	public void setComplexSpectrum(){
		currentSpectrumLength = PamUtils.getMinFftLength(getSampleDuration());
	}

	/**
	 * 
	 * Returns the complex spectrum for a given channel using a set FFT length
	 * 
	 * @param channel
	 * @param fftLength
	 * @return the complex spectrum
	 */
	public synchronized ComplexArray getComplexSpectrum(int channel, int fftLength) {
		return rawdataTransforms.getComplexSpectrum(channel, fftLength); 
	}


	/**
	 * 
	 * Returns the complex spectrum for a given channel using a set FFT length as
	 * getComplexSpectrum, but applies a Hanning window to the raw data first 
	 * 
	 * @param channel
	 * @param fftLength
	 * @return the complex spectrum
	 */
	public synchronized ComplexArray getComplexSpectrumHann(int channel, int fftLength) {
		return rawdataTransforms.getComplexSpectrumHann(channel, fftLength); 
	}

//	/**
//	 *  Returns the complex spectrum for waveform using a set FFT length
//	 * @param waveData
//	 * @return the complex Spectrum
//	 */
//	public static Complex[] getComplexSpectrum(double[] rawData, int fftLength){
//		double[] paddedRawData;
//		int i, mn;
//
//
//		paddedRawData = new double[fftLength];
//		mn = Math.min(fftLength, rawData.length);
//		for (i = 0; i < mn; i++) {
//			paddedRawData[i] = rawData[i];
//		}
//		for (i = mn; i < fftLength; i++) {
//			paddedRawData[i] = 0;
//		}
//		FastFFT fft=new FastFFT();
//
//		Complex[] c = fft.rfft(paddedRawData, null,
//				PamUtils.log2(fftLength));
//
//
//		return c;
//	}

	@Override
	public List<FFTDataUnit> getFFTDataUnits(Integer fftLength) {
		ArrayList<FFTDataUnit> fftList = new ArrayList<>(nChan);
		ComplexArray complexData;
		for (int i = 0; i < nChan; i++) {
			if (fftLength != null) {
				complexData = getComplexSpectrum(i, fftLength);
			}
			else {
				complexData = getComplexSpectrum(i);
				fftLength = complexData.length()*2;
			}
			if (complexData != null) {
				FFTDataUnit fftDataUnit = new FFTDataUnit(getTimeMilliseconds(), 1<<PamUtils.getNthChannel(i, getChannelBitmap()), 
						getStartSample(), getSampleDuration(), complexData, i);
				fftList.add(fftDataUnit);
			}
		}
		return fftList.size() > 0 ? fftList : null;
	}

	/**
	 * Find out whether there are complex 
	 * spectrum data - and if there are, data may
	 * get cleaned up. 
	 * @return true if complex spec data exist. 
	 */
	public boolean hasComplexSpectrum() {
		return (this.rawdataTransforms.getCurrentComplexSpectra() != null);
	}

	public int getCurrentSpectrumLength() {
		return currentSpectrumLength;
	}

	/**
	 * Get the power spectrum of the entire click. 
	 * @param channel channel number
	 * @return power spectrum
	 */
	public double[] getPowerSpectrum(int channel) {
		int fftLen = FastFFT.nextBinaryExp(getSampleDuration().intValue());
		return getPowerSpectrum(channel, fftLen);
	}

	/**
	 * Get the power spectrum of the entire click. Note, if recalc is false then this function can return null. 
	 * @param channel - channel number
	 * @param noRecalc - true to calculate power spectrum if it's null or fft length is not the same.  
	 * @return the click power spectrum.
	 */
	public double[] getPowerSpectrum(int channel, boolean recalc) {
		if (!recalc && this.rawdataTransforms.getCurrentPowerSpectra()!=null) return rawdataTransforms.getCurrentPowerSpectra()[channel];
		else if (!recalc && rawdataTransforms.getCurrentPowerSpectra()==null) return null;
		else return  getPowerSpectrum(channel) ; 
	}

	/**
	 * Calculate the cepstrum for a click channel. 
	 * @param channel cannel
	 * @param cepLength length of cepstrum. If this is 0, then the Cepstrum 
	 * will be the next binary int up from the click length. 
	 * @return cepstrum. (ifft of the log of the power spectrum)
	 */
	public double[] getCepstrum(int channel, int cepLength) {
		if (cepLength == 0) {
			cepLength = FastFFT.nextBinaryExp(this.getSampleDuration().intValue());
		}
		int fftLength = cepLength * 2;
		//		int logFFTLength = FastFFT.log2(fftLength);
		ComplexArray spec = getComplexSpectrum(channel, fftLength);
		// the complex spec will have been returned at half the fft length. We need the whole thing
		// back and also it's log amplitude !
		ComplexArray specData = new ComplexArray(fftLength);
		double a;
		for (int i = 0, j = fftLength-1; i < fftLength/2; i++, j--) {
			specData.set(i, a=Math.log(spec.magsq(i)), 0);
			specData.set(j, a, 0);
		}
		getClickDetector().cepstrumFFT.ifft(spec, fftLength);
		double[] cepstrum = new double[cepLength];
		for (int i = 3; i < cepLength; i++) {
			cepstrum[i] = spec.getReal(i);
		}
		return cepstrum;
	}

	/**
	 * Returns the power spectrum for a given channel (square of magnitude of
	 * complex spectrum)
	 * 
	 * @param channel channel number
	 * @param fftLength
	 * @return Power spectrum
	 */
	public synchronized double[] getPowerSpectrum(int channel, int fftLength) {
		return this.rawdataTransforms.getPowerSpectrum(channel, fftLength); 
//		if (powerSpectra == null) {
//			powerSpectra = new double[nChan][];
//		}
//		if (fftLength == 0) {
//			fftLength = getCurrentSpectrumLength();
//			//System.out.println("Current specturm length = " + fftLength);
//		}
//		if (fftLength == 0) {
//			//fftLength = PamUtils.getMinFftLength(getSampleDuration());
//		}
//		if (powerSpectra[channel] == null
//				|| powerSpectra[channel].length != fftLength / 2) {
//			ComplexArray cData = getComplexSpectrum(channel, fftLength);
//			currentSpectrumLength = fftLength;
//			powerSpectra[channel] = cData.magsq();
//			if (powerSpectra==null){
//				System.err.println("ClickDetection: could not calculate power spectra");
//				return null;
//
//			}
//			if (powerSpectra[channel].length != fftLength/2) {
//				powerSpectra[channel] = Arrays.copyOf(powerSpectra[channel], fftLength/2);
//			}
//		}
//
//		//		for (int i=0; i<powerSpectra[channel].length; i++){
//		//			System.out.println("ClickSpectrum: "+i+ " "+powerSpectra[channel][i]);
//		//		}
//
//		return powerSpectra[channel];
	}

	/**
	 * Returns the magnitude of the complex spectrum for a given channel.  Has the
	 * option to apply a hanning window to the raw data before computing the fft
	 * 
	 * @param channel
	 * @param fftLength
	 * @param hann TRUE=apply hanning window; FALSE=do not apply hanning window
	 * @return
	 */
	public synchronized double[] getMagnitude(int channel, int fftLength, boolean hann) {
		if (fftLength == 0) {
			fftLength = getCurrentSpectrumLength();
		}
		if (fftLength == 0) {
			fftLength = PamUtils.getMinFftLength(getSampleDuration());
		}
		ComplexArray cData;
		if (hann) {
			cData = getComplexSpectrumHann(channel, fftLength);
		} else {
			cData = getComplexSpectrum(channel, fftLength);
		}
		double[] magnitude = cData.mag();
		if (magnitude.length != fftLength/2) {
			magnitude = Arrays.copyOf(magnitude, fftLength/2);
		}
		//		
		//		double[] magnitude = new double[fftLength / 2];
		//		for (int i = 0; i < fftLength / 2; i++) {
		//			magnitude[i] = cData[i].mag();
		//		}
		return magnitude;
	}

	/**
	 * Returns the sum of the power spectra for all channels
	 * 
	 * @param fftLength
	 * @return Sum of power spectra
	 */
	public synchronized double[] getTotalPowerSpectrum(int fftLength) {
		return this.rawdataTransforms.getTotalPowerSpectrum(fftLength); 
//		if (fftLength == 0) {
//			fftLength = getCurrentSpectrumLength();
//		}
//		if (fftLength == 0) {
//			fftLength = PamUtils.getMinFftLength(getSampleDuration());
//		}
//		double[] ps;
//		if (totalPowerSpectrum == null
//				|| totalPowerSpectrum.length != fftLength / 2) {
//			totalPowerSpectrum = new double[fftLength / 2];
//			for (int c = 0; c < nChan; c++) {
//				ps = getPowerSpectrum(c, fftLength);
//				for (int i = 0; i < fftLength / 2; i++) {
//					totalPowerSpectrum[i] += ps[i];
//				}
//			}
//		}
//		return totalPowerSpectrum;
	}

	/**
	 * Get the analytic waveform for  a given channel
	 * @param iChan channel index
	 * @return analytic waveform
	 */
	public synchronized double[] getAnalyticWaveform(int iChan) {
		return this.rawdataTransforms.getAnalyticWaveform(iChan); 
//		if (analyticWaveform == null) {
//			analyticWaveform = new double[nChan][];
//		}
//		//		if (analyticWaveform[iChan] == null) {
//		analyticWaveform[iChan] = getClickDetector().getHilbert().getHilbert(getWaveData(iChan));
//		//		}
//		return analyticWaveform[iChan];
	}

	/**
	 * Get filtered or unfiltered analytic waveform. Easy access method for click 
	 * detector modules which can let this function work out what they want rather 
	 * than having to write their own. 
	 * @param iChan channel number
	 * @param filtered true if you want data to be filtered
	 * @param fftFilterParams fft filter parameters. 
	 * @return analystic waveform. 
	 */
	public synchronized double[] getAnalyticWaveform(int iChan, boolean filtered, FFTFilterParams fftFilterParams) {
		if (!filtered || fftFilterParams == null) {
			return getAnalyticWaveform(iChan);
		}
		else {
			return getFilteredAnalyticWaveform(fftFilterParams, iChan);
		}
	}

	/**
	 * Get a filtered version of the analytic waveform. In principle, this could be made more efficient
	 * since the calc is done partly in frequency domain - so could save a couple of fft's back and forth. 
	 * @param fftFilterParams FFT filter parameters. 
	 * @param iChan channel number
	 * @return envelope of the filtered data. 
	 */
	public synchronized double[] getFilteredAnalyticWaveform(FFTFilterParams fftFilterParams, int iChan) {
		return this.rawdataTransforms.getFilteredAnalyticWaveform(fftFilterParams, iChan); 
//		if (analyticWaveform == null) {
//			analyticWaveform = new double[nChan][];
//		}
//		//		if (analyticWaveform[iChan] == null) {
//		analyticWaveform[iChan] = getClickDetector().getHilbert().
//				getHilbert(getFilteredWaveData(fftFilterParams, iChan));
//		//		}
//		return analyticWaveform[iChan];
	}

	/**
	 * Get the analytic waveform for all channels 
	 * if filter params = null, then return normal analytic waveform  
	 * @param fftFilterParams
	 * @return analystic waveforms 
	 */
	public double[][] getFilteredAnalyticWaveform(FFTFilterParams fftFilterParams) {
		return this.getFilteredAnalyticWaveform(fftFilterParams); 
//		if (analyticWaveform == null) {
//			analyticWaveform = new double[nChan][];
//		}
//		for (int iChan = 0; iChan < nChan; iChan++) {
//			if (fftFilterParams != null) {
//				analyticWaveform[iChan] = getClickDetector().getHilbert().
//						getHilbert(getFilteredWaveData(fftFilterParams, iChan));
//			}
//			else {
//				analyticWaveform[iChan] = getAnalyticWaveform(iChan);
//			}
//		}
//		return analyticWaveform;
	}



	/**
	 * Calculates the total energy within a particular frequency band
	 * 
	 * @see BasicClickIdentifier
	 * @param freqs
	 * @return In Band Energy
	 */
	public double inBandEnergy(double[] freqs) {
		double e = 0;
		int fftLen = getShortestFFTLength();
		double[][] specData = new double[nChan][];
		for (int i = 0; i < nChan; i++) {
			specData[i] = getPowerSpectrum(i, fftLen);
		}
		int f1 = Math.max(0, (int) Math.floor(freqs[0] * fftLen
				/ getClickDetector().getSampleRate()));
		int f2 = Math.min((fftLen / 2) - 1, (int) Math.ceil(freqs[1]
				* fftLen / getClickDetector().getSampleRate()));
		for (int iChan = 0; iChan < nChan; iChan++) {
			for (int f = f1; f <= f2; f++) {
				e += specData[iChan][f];
			}
		}
		if (e > 0.) {
			return 10 * Math.log10(e) + 172;
		} else
			return -100;
	}

	/**
	 * Calculates the length of a click in seconds averaged over all channels
	 * 
	 * @see BasicClickIdentifier
	 * @param percent
	 *            Fraction of total click energy to use in the calculation
	 * @return click length in seconds
	 */
	public double clickLength(double percent) {
		/*
		 * work out the length of the click - this first requries a bit of
		 * smoothing out of the rectified waveform, then an iterative search
		 * around either side of the peak, then average for all channels
		 */
		double sum = 0;
		for (int i = 0; i < nChan; i++) {
			sum += clickLength(i, percent);
		}
		return sum / nChan;
	}

	/**
	 * Calculates the length of a click in seconds for a particular channel
	 * 
	 * @see BasicClickIdentifier
	 * @param channel
	 * @param percent
	 *            Fraction of total click energy to use in the calculation
	 * @return Click Length (seconds)
	 */
	public double clickLength(int channel, double percent) {
		int length = 0;
		int nAverage = 3;
		double[] waveData = getWaveData(channel);
		double[] smoothData = new double[waveData.length];
		double squaredData;
		double totalData = 0;
		double dataMaximum = 0;
		int maxPosition = 0;
		for (int i = 0; i < smoothData.length; i++) {
			smoothData[i] = Math.pow(waveData[i], 2);
		}
		for (int i = 0; i < smoothData.length - nAverage; i++) {
			for (int j = 1; j < nAverage; j++) {
				smoothData[i] += smoothData[i + j];
			}
			totalData += smoothData[i];
			if (smoothData[i] > dataMaximum) {
				dataMaximum = smoothData[i];
				maxPosition = i;
			}
		}
		/*
		 * Now start at the maximum position and search out back and forwards
		 * until enough energy has been found use a generic peakwidth function
		 * for this, since it's the same basic process that does the width of
		 * the frequency peak
		 */
		length = getSpikeWidth(smoothData, maxPosition, percent);
		return length / getClickDetector().getSampleRate();
	}

	/**
	 * Calculates the width of a peak - either time or frequency data
	 * 
	 * @param data
	 * @param peakPos
	 * @param percent
	 * @return Width of spike in whatever bins are used for raw data given
	 */
	private int getSpikeWidth(double[] data, int peakPos, double percent) {
		/*
		 * This is used both by the length measuring and the frequency peak
		 * measuring functions
		 */
		int width = 1;
		int len = data.length;
		double next, prev;
		int inext, iprev;
		double targetEnergy = 0;
		if (percent >= 100) { // MO 2014/09/25 changed from ">" to ">="
			return len;
		}
		for (int i = 0; i < len; i++) {
			targetEnergy += data[i];
		}
		targetEnergy *= percent / 100;
		double foundEnergy = data[peakPos];
		inext = peakPos + 1;
		iprev = peakPos - 1;
		while (foundEnergy < targetEnergy) {
			next = prev = 0;
			if (inext < len)
				next = data[inext];
			if (iprev >= 0)
				prev = data[iprev];
			if (next > prev) {
				foundEnergy += next;
				inext++;
				width++;
			} else if (next < prev) {
				foundEnergy += prev;
				iprev--;
				width++;
			} else {
				foundEnergy += (next + prev);
				inext++;
				iprev--;
				width += 2;
			}
			if (iprev < 0 && inext >= len) {
				System.out.println("Can't find required energy in click");
			}
		}

		return width;
	}

	/**
	 * Calculates the width of a peak based on a drop in dB to either side of
	 * the peak frequency
	 * 
	 * @param data the power spectrum to analyze
	 * @param peakPos the bin containing the peak frequency
	 * @param dBDrop the drop in dB to either side of the peak frequency.
	 * @return integer array containing (in order): width of peak in bins, lowest bin, highest bin
	 */
	private int[] getSpikeWidth(double[] data, int peakPos, int dBDrop) {
		int[] width = {0,0,0};
		int inext, iprev;
		double drop=Math.pow(10, Math.abs(dBDrop)/20.0);	// MO 2014/07/17 changed 20 to 20.0 to force double instead of int
		double targetPower = data[peakPos]/drop;			// MO 2014/07/17 changed subtraction to division (subtraction would only be correct if variables were in dB)
		inext = peakPos + 1;
		iprev = peakPos - 1;

		// step up through the array one position at a time and compare the energy to
		// the target energy
		// 2014-07-01 MO added check to make sure we don't go past end of array
		while (inext<(data.length-1) && data[inext]>targetPower) {
			inext++;
		}

		// step down through the array one position at a time and compare the energy to
		// the target energy
		// 2014-07-01 MO added check to make sure we don't go past beginning of array
		while (iprev>0 && data[iprev]>targetPower) {
			iprev--;
		}

		// load data into the return variable
		width[0]=inext-iprev;
		width[1]=iprev;
		width[2]=inext;
		return width;
	}

	public synchronized double peakFrequency(double[] searchRange) {
		/*
		 * search range will be in Hz, so convert to bins NB - the
		 */
		int fftLength = getShortestFFTLength();
		double[] powerSpec = getTotalPowerSpectrum(fftLength);

		int bin1 = (int) Math.max(0, Math.floor(searchRange[0] * fftLength
				/ getClickDetector().getSampleRate()));
		int bin2 = (int) Math.min(fftLength / 2 - 1, Math.ceil(searchRange[1]
				* fftLength / getClickDetector().getSampleRate()));
		int peakPos = 0;
		double peakEnergy = 0;
		for (int i = bin1; i <= bin2; i++) {
			if (powerSpec[i] > peakEnergy) {
				peakEnergy = powerSpec[i];
				peakPos = i;
			}
		}
		return peakPos * getClickDetector().getSampleRate() / fftLength;
	}

	public double peakFrequencyWidth(double peakFrequency, double percent) {
		int fftLength = getShortestFFTLength();
		int peakPos = (int) (peakFrequency * fftLength / getClickDetector()
				.getSampleRate());
		int width = getSpikeWidth(getTotalPowerSpectrum(fftLength), peakPos,
				percent);
		return width * getClickDetector().getSampleRate() / fftLength;
	}

	public double[] peakFrequencyWidth(double peakFrequency, int dBDrop) {
		int fftLength = getShortestFFTLength();
		int peakPos = (int) (peakFrequency * fftLength / getClickDetector()
				.getSampleRate());
		int width[] = getSpikeWidth(getTotalPowerSpectrum(fftLength), peakPos,
				dBDrop);
		double[] widthInHz = {0,0,0};
		widthInHz[0]=width[0] * getClickDetector().getSampleRate() / fftLength;
		widthInHz[1]=width[1] * getClickDetector().getSampleRate() / fftLength;
		widthInHz[2]=width[2] * getClickDetector().getSampleRate() / fftLength;
		return widthInHz;
	}

	/**
	 * Returns the frequency width of a drop in dBDrop decibels to either side of the peak frequency.
	 * Note that this differs from the method peakFrequencyWidth because it operates only on a single channel, 
	 * and it uses the magnitude (sqrt(PowerSpectrum)) instead of the PowerSpectrum.
	 * 
	 * @param peakFrequency The peak frequency in Hz
	 * @param dBDrop The drop in magnitude (in dB) to either side of the peak frequency
	 * @param channel The channel to run the calculations on
	 * @param hann TRUE=apply hanning window, FALSE=do not apply hanning window
	 * 
	 * @return a 3-position array with the frequency bin in the first index, the lower frequency in the
	 * second index, and the higher frequency in the third index
	 */
	public double[] peakFrequencyWidthChan(double peakFrequency, int dBDrop, int channel, boolean hann) {
		int fftLength = getShortestFFTLength();
		int peakPos = (int) (peakFrequency * fftLength / getClickDetector()
				.getSampleRate());
		int width[];
		width = getSpikeWidth(getMagnitude(channel,fftLength,hann), peakPos,
				dBDrop);
		double[] widthInHz = {0,0,0};
		widthInHz[0]=width[0] * getClickDetector().getSampleRate() / fftLength;
		widthInHz[1]=width[1] * getClickDetector().getSampleRate() / fftLength;
		widthInHz[2]=width[2] * getClickDetector().getSampleRate() / fftLength;
		return widthInHz;
	}


	public synchronized double getMeanFrequency(double[] searchRange) {

		int fftLength = getShortestFFTLength();
		double[] powerSpec = getTotalPowerSpectrum(fftLength);

		int bin1 = (int) Math.max(0, Math.floor(searchRange[0] * fftLength
				/ getClickDetector().getSampleRate()));
		int bin2 = (int) Math.min(fftLength / 2 - 1, Math.ceil(searchRange[1]
				* fftLength / getClickDetector().getSampleRate()));
		double top = 0, bottom = 0;
		for (int i = bin1; i <= bin2; i++) {
			top += (i * powerSpec[i]);
			bottom += powerSpec[i];
		}
		double meanFreq = top / bottom; // mean Freq in bins
		return meanFreq * getClickDetector().getSampleRate() / fftLength;
	}

	/**
	 * Get filtered waveform data for a single channel. <p>
	 * Data are filtered in the frequency domain using an FFT / Inverse FFT. 
	 * @param filterParams filter parameters
	 * @param channelIndex channel index
	 * @return filtered waveform data
	 */
	public synchronized double[] getFilteredWaveData(FFTFilterParams filterParams, int channelIndex) {
		return this.rawdataTransforms.getFilteredWaveData(filterParams, channelIndex); 
//		filteredWaveData = getFilteredWaveData(filterParams);
//		return filteredWaveData[channelIndex];
	}

	/**
	 * Get filtered waveform data for all channels. <p>
	 * Data are filtered in the frequency domain using an FFT / Inverse FFT. 
	 * @param filterParams filter parameters
	 * @return array of filtered data
	 */
	public synchronized double[][] getFilteredWaveData(FFTFilterParams filterParams) {
		return this.rawdataTransforms.getFilteredWaveData(filterParams); 
//		//System.out.println("Make filterred wave data!: " + (filterParams != oldFFTFilterParams));
//		if (filteredWaveData == null || filterParams != oldFFTFilterParams) {
//			filteredWaveData = makeFilteredWaveData(filterParams);
//		}
//		return filteredWaveData;
	}

//	private FFTFilterParams oldFFTFilterParams;
	
//	private double[][] makeFilteredWaveData(FFTFilterParams filterParams) {
//		double[][] waveData = getWaveData();
//		if (waveData == null || waveData[0].length == 0) {
//			return null;
//		}
//		
//		// now make a zeroed copy of it all. 
//		double rotData[][] = new double[waveData.length][waveData[0].length];
//		for (int iChan = 0; iChan < waveData.length; iChan++) {
//			double[] rotCorr = getRotationCorrection(iChan);
//			for (int iSamp = 0; iSamp < waveData[0].length; iSamp++) {
//				rotData[iChan][iSamp] = waveData[iChan][iSamp] - rotCorr[iSamp];
//			}
//		}
//		int nChan = waveData.length;
//		int dataLen = waveData[0].length;
//		filteredWaveData = new double[nChan][dataLen];
//		FFTFilter filter = getClickDetector().getFFTFilter(filterParams);
//		for (int i = 0; i < nChan; i++) {
//			filter.runFilter(rotData[i], filteredWaveData[i]);
//		}
//		oldFFTFilterParams = filterParams;
//		return filteredWaveData;
//	}

	/**
	 * convenience method to get filtered or unfiltered data for a single channel.
	 * @param filtered flag saying you want it filtered
	 * @param fftFilterParams filter parameters. 
	 * @return data filtered or otherwise. 
	 */
	public double[][] getWaveData(boolean filtered, FFTFilterParams fftFilterParams) {
		if (!filtered || fftFilterParams == null) {
			return getWaveData();
		}
		else {
			return getFilteredWaveData(fftFilterParams);
		}
	}
	/**
	 * convenience method to get filtered or unfiltered data for a single channel. 
	 * @param channelIndex channel index
	 * @param filtered flag saying you want it filtered
	 * @param fftFilterParams filter parameters. 
	 * @return data filtered or otherwise. 
	 */
	public double[] getWaveData(int channelIndex, boolean filtered, FFTFilterParams fftFilterParams) {
		if (!filtered || fftFilterParams == null) {
			return getWaveData(channelIndex);
		}
		else {
			return getFilteredWaveData(fftFilterParams, channelIndex);
		}
	}

	/**
	 * Get raw waveform data for a given click channel index. 
	 * @param channelIndex channel index
	 * @return waveform data
	 */
	public synchronized double[] getWaveData(int channelIndex) {
		double[][] wD = getWaveData();
		if (wD == null) {
			return null;
		}
		return wD[channelIndex];
	}

	/**
	 * 
	 * @return waveform data for all channels. Convert from compressed (int16) data 
	 * if necessary. 
	 */
	@Override
	public synchronized double[][] getWaveData() {
		if (waveData != null) {
			return waveData;
		}
		if (compressedWaveData == null) {
			return null;
		}

		waveAmplitude = getWaveAmplitude();

		int nChan = compressedWaveData.length;
		int nSamp = compressedWaveData[0].length;
		waveData = new double[nChan][nSamp];

		for (int i = 0; i < nChan; i++) {
			double scale = waveAmplitude/127.;
			if (SMRUEnable.isMeygen17(getTimeMilliseconds())) {
				// work out which channel it is !
				int iChan = PamUtils.getNthChannel(i, getChannelBitmap());
				int goodChannels = SMRUEnable.getGoodChannels(getChannelBitmap());
					if ((1<<iChan & goodChannels) == 0) {
						scale /= 100.;
					}
			}
//			if (meygenChannelBodge(nChan, i, this.getTimeMilliseconds())) {
//				/*
//				 * Bodge to handle inverted channels in Meygen data, October 2017. 
//				 */
//				scale = -scale;
//			}
			for (int j = 0; j < nSamp; j++) {
				waveData[i][j] = scale * compressedWaveData[i][j];
			}
			//			rotateWaveData(waveData[i]);
		}



		return waveData;
	}

	/**
	 * Had to bodge the sign of the data on some channels a little for the first 
	 * week of the Meygen data collection. This was the last channel in each group 
	 * and lasted from the start of DAQ on 19/10/2018 until 08:20UTC on 27/10/2017
	 * @param nChan total number of channels in click (always 4
  	 * @param i channel index
	 * @param timeMilliseconds
	 * @return true if sign should be flipped. 
	 */
	private boolean meygenChannelBodge(int nChan, int i, long timeMilliseconds) {
		if (nChan != 4) {
			return false;
		}
		if (timeMilliseconds > 1509092400000L || timeMilliseconds < 1508371200000L) {
			return false;
		}
		return ((i+1)%4 == 0);
	}


	/**
	 * Rotate a waveform so that the ends are both at zero. 
	 * @param waveform from a single channel 
	 */
	private void rotateWaveData(double[] w) {
		int n = w.length;
		double b = w[0];
		double a = (w[n-1]-b)/(n-1);
		for (int i = 0; i < n; i++) {
			w[i] -= (a*i + b);
		}
		//		System.out.println("Rotate wave data !");
	}


	public void setWaveData(double[][] waveData) {
		this.waveData = waveData;
	}

	/**
	 * Get compressed waveform data in int8 format, scaled
	 * so that the maximum range >-127 to +127 is utilised. 
	 * @return arrays of waveform data. 
	 */
	public byte[][] getCompressedWaveData() {
		if (compressedWaveData != null) {
			return compressedWaveData;
		}
		// otherwise, create the compressed data ...
		if (waveData == null) {
			return null;
		}

		waveAmplitude = getWaveMax(waveData);

		int nChan = waveData.length;
		int nSamp = waveData[0].length;
		compressedWaveData = new byte[nChan][nSamp];

		double scale = 127./waveAmplitude;
		for (int i = 0; i < nChan; i++) {
			for (int j = 0; j < nSamp; j++) {
				compressedWaveData[i][j] = (byte) (scale * waveData[i][j]);
			}
		}

		return compressedWaveData;
	}

	/**
	 * Set the compressed wave data (used when reading back from file).
	 * @param compressedWaveData
	 * @param waveAmplitude
	 */
	public void setCompressedData(byte[][] compressedWaveData, double waveAmplitude) {
		this.compressedWaveData = compressedWaveData;
		this.waveAmplitude = waveAmplitude;
	}

	/**
	 * @return the waveAmplitude - the double precision amplitude of the orignal
	 * wave data. 
	 */
	public double getWaveAmplitude() {
		return waveAmplitude;
	}

	/**
	 * Get the length of the wave in samples. 
	 * @return the length of the wave data in samples. 
	 */
	public int getWaveLength() {
		if (waveData!=null && waveData.length>0) return waveData[0].length;
		else return 0;
	}

	/**
	 * get the maximum value of the wavedata. Will be used for scaling. 
	 * @param waveData wavedata 2D array
	 * @return maximum absolute value. 
	 */
	private double getWaveMax(double[][] waveData) {
		if (waveData == null) {
			return 0;
		}
		int l1 = waveData.length;
		if (l1 == 0) {
			return 0;
		}
		int l2 = waveData[0].length;
		if (l2 == 0) {
			return 0;
		}
		double max = 0;
		for (int i = 0; i < l1; i++) {
			for (int j = 0; j < l2; j++) {
				max = Math.max(max, Math.abs(waveData[i][j]));
			}
		}
		return max;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#freeData()
	 */
	@Override
	public void freeData() {
		freeClickMemory();
	}
	
	/**
	 * Free up the most memory - basically clear everything except for
	 * the compressed wave data held in field compressedWaveData.  That we
	 * need, but everything else can be recalculated if necessary.
	 */
	public void freeMaxMemory() {
		this.freeClickMemory();
		this.rawdataTransforms.freeMemory(); 
//		filteredWaveData = null;
//		powerSpectra = null;
//		analyticWaveform = null;
	}


	/**
	 * Free up as much click memory as possible. <p>
	 * Ensures that waveform data are retained in a compressed (int8) format
	 * so that all other data can be reconstructed if necessary. 
	 */
	public synchronized void freeClickMemory() {
		//		waveData = null;
		//powerSpectra = null;
		this.rawdataTransforms.setComplexSpectrum(null); 
		
		this.rawdataTransforms.freeMemory(); 

		/*
		 * Check the double waveform data can convert into a byte
		 * array, then get rid of it. In reality, this conversion was
		 * probably already done when the click was written to binary file.  
		 */
		if (getCompressedWaveData() != null) {
			waveData = null;
		}
	}
	//
	//	public double getMeanAmplitude()
	//	{
	//		double a = 0;
	//		for (int i = 0; i < amplitude.length; i++) {
	//			a += amplitude[i];
	//		}
	//		return a / amplitude.length;
	//	}
	//	/**
	//	 * @return Returns the dBamplitude.
	//	 */
	//	public double getDBamplitude() {
	//		return dBamplitude;
	//	}
	//
	//	/**
	//	 * @param bamplitude The dBamplitude to set.
	//	 */
	//	public void setDBamplitude(double bamplitude) {
	//		dBamplitude = bamplitude;
	//	}
	//
	//	/**
	//	 * @return Returns the delay.
	//	 */
	//	public int getDelay() {
	//		return delay;
	//	}
	/**
	 * Set the time of arrival delay in samples.
	 * @param delay delay in samples
	 */
	public void setDelayInSamples(int iDelay, double delay) {
		if (getClickLocalisation() == null) {
			makeClickLocalisation(0, null);
		}
		if (delaysInSamples == null) {
			delaysInSamples = new double[iDelay+1];
		}
		if (iDelay >= delaysInSamples.length) {
			delaysInSamples = Arrays.copyOf(delaysInSamples, iDelay+1);
		}
		delaysInSamples[iDelay] = delay;
		if (iDelay == 0) {
			clickLocalisation.setFirstDelay((int) delay);
		}
		clickLocalisation.addLocContents(LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY);

		// also save the delaysInSamples in the DataUnitBaseData object, but convert to time first
		double[] delaysInSec = new double[delaysInSamples.length];
		for (int i=0; i<delaysInSamples.length; i++) {
			delaysInSec[i]=delaysInSamples[i]/clickDetector.getSampleRate();
		}
		this.setTimeDelaysSeconds(delaysInSec);
	}


	public void setDelaysInSamples(double[] delays) {
		if (getClickLocalisation() == null) {
			makeClickLocalisation(0, null);
		}
		this.delaysInSamples = delays;
		if (delays != null && delays.length > 0) {
			clickLocalisation.setFirstDelay(delays[0]); 
			clickLocalisation.addLocContents(LocContents.HAS_BEARING);
			if (delays.length == 1) {
				clickLocalisation.addLocContents(LocContents.HAS_AMBIGUITY);
			}
			else {
				clickLocalisation.removeLocContents(LocContents.HAS_AMBIGUITY);
			}
		}

		// also save the delaysInSamples in the DataUnitBaseData object, but convert to time first
		double[] delaysInSec = new double[delays.length];
		for (int i=0; i<delays.length; i++) {
			delaysInSec[i]=delays[i]/clickDetector.getSampleRate();
		}
		this.setTimeDelaysSeconds(delaysInSec);
	}

	/**
	 * return a list of delaysInSamples. 
	 * @return
	 */
	public double[] getDelaysInSamples() {
		return delaysInSamples;
	}

	/**
	 * Calculate the peak waveform amplitude
	 * making the correction of rotating the wave so it's two ends
	 * are at zero.
	 * @param channel
	 * @return true if calculation completed OK. 
	 */
	public boolean calculateAmplitude(int channel) {
		double[] chanData = getWaveData(channel);
		double[] correction = getRotationCorrection(channel);
		if (chanData == null || correction == null) return false;
		double amp = 0;
		for (int i = 0; i < chanData.length; i++) {
			amp = Math.max(amp, Math.abs(chanData[i]-correction[i]));
		}
		setAmplitude(channel, amp);
		return true;
	}

	/**
	 * Get a correction based on the slope of the waveform which 
	 * can be used to remove large DC / LF offsets in the waveform. 
	 * @param channel - the channel to correct
	 * @return the corrected waveform
	 */
	public double[] getRotationCorrection(int channel) {
		double[] chanData = getWaveData(channel);
		if (chanData == null) return null;
		int len = chanData.length;
		double[] correction = new double[len];
		correction[0] = chanData[0];
		if (len == 1) {
			return correction;
		}
		double slope = (chanData[len-1] - chanData[0]) / (len-1);
		for (int i = 1; i < len; i++) {
			correction[i] = chanData[0] + slope*i;
		}
		return correction;
	}
	
	/**
	 * Set the amplitude for a given channel
	 * @param channel channel number
	 * @param amplitude amplitude
	 */
	public void setAmplitude(int channel, double amplitude) {
		if (this.amplitude == null) {
			this.amplitude = new double[1];
		}
		if (this.amplitude.length <= channel) {
			this.amplitude = Arrays.copyOf(this.amplitude, channel+1);
		}
		this.amplitude[channel] = amplitude;
	}

	public double getAmplitude(int channel) {
		return amplitude[channel];
	}

	/**
	 * Returns the angle in degrees for compatibilty with older version of click detector
	 * This is really bad to use for anything apart from two element arrays and it would be
	 * sensible to remove the function entirely. 
	 * @return angle of the click detection in degrees
	 */
	public double getAngle() {
		double angle = 0;
		if (getClickLocalisation() != null) {
			angle = getClickLocalisation().getBearing(0) * 180 / Math.PI;
		}
		return angle;
	}

	public double getMeanAmplitude()
	{
		double a = 0;
		for (int i = 0; i < amplitude.length; i++) {
			a += amplitude[i];
		}
		return a / amplitude.length;
	}

	private void makeClickLocalisation(int arrayType, PamVector[] arrayAxes) {
		int hydrophoneList = getChannelBitmap();
		if (clickDetector != null && clickDetector.getParentDataBlock() != null) {
			ChannelListManager clm = ((PamRawDataBlock) clickDetector.getParentDataBlock()).getChannelListManager();
			if (clm != null) {
				hydrophoneList = clm.channelIndexesToPhones(getChannelBitmap());
			}
		}
		//		AcquisitionControl dc = findDaqControl();
		//		if (dc != null) {
		//			hydrophoneList = dc.ChannelsToHydrophones(channelBitmap);
		//		}
		BearingLocaliser bearingLocaliser = null;
		if (getChannelGroupDetector() != null) {
			getChannelGroupDetector().getBearingLocaliser();
		}
		if (bearingLocaliser != null) {
			setClickLocalisation(new ClickLocalisation(this, 0, hydrophoneList,
					getChannelGroupDetector().getBearingLocaliser().getArrayType(),
					getChannelGroupDetector().getBearingLocaliser().getArrayAxis()));
		}
		else {
			setClickLocalisation(new ClickLocalisation(this, 0, hydrophoneList, 
					arrayType, arrayAxes));
		}
	}

	private void setReferenceHydrophones(int channelMap) {
		int hydrophoneList = channelMap;
		AcquisitionControl dc = findDaqControl();
		if (dc != null) {
			hydrophoneList = dc.ChannelsToHydrophones(getChannelBitmap());
		}
		if (clickLocalisation != null) {
			clickLocalisation.setReferenceHydrophones(hydrophoneList);
		}
	}

	private AcquisitionControl findDaqControl() {
		if (getClickDetector() == null) return null;
		if (getClickDetector().getSourceProcess() == null) return null;
		try {
			return ((AcquisitionProcess) getClickDetector().getSourceProcess()).
					getAcquisitionControl();
		}
		catch (ClassCastException e) {
			return null;
		}
	}

	public ClickLocalisation getClickLocalisation() {
		return clickLocalisation;
	}

	public void setClickLocalisation(ClickLocalisation clickLocalisation) {
		this.clickLocalisation = clickLocalisation;
		this.localisation = clickLocalisation;
	}

//	@Override
//	public AbstractDetectionMatch getDetectionMatch(int type){
//		if (clickDetectionMatch!=null && clickDetectionMatch.getClickType()==type ){
//			return clickDetectionMatch;
//		}
//		else{
//			clickDetectionMatch=new ClickDetectionMatch(this,type);
//			return clickDetectionMatch;
//		}
//	}

//	@Override
//	public AbstractDetectionMatch getDetectionMatch(){
//		if (clickDetectionMatch!=null){
//			return clickDetectionMatch;
//		}
//		else{
//			clickDetectionMatch=new ClickDetectionMatch(this);
//			return clickDetectionMatch;
//		}
//	}



	public int getNChan() {
		return PamUtils.getNumChannels(getChannelBitmap());
	}



	@Override
	public void setChannelBitmap(int channelBitmap) {
		this.nChan = PamUtils.getNumChannels(channelBitmap);
		setReferenceHydrophones(channelBitmap);
		super.setChannelBitmap(channelBitmap);
	}

	/**
	 * @param clickType the clickType (click species) to set
	 */
	public void setClickType(byte clickType) {
		this.clickType = clickType;
	}

	/**
	 * @return the clickType (click species)
	 */
	public byte getClickType() {
		return clickType;
	}

	/**
	 * 
	 * @return the type of data - click, noise, etc. 
	 */
	public byte getDataType() {
		return dataType;
	}

	/**
	 * 
	 * @param dataType the type of data - click, noise, etc. 
	 */
	public void setDataType(byte dataType) {
		this.dataType = dataType;
	}

	/**
	 * @param clickDetector the clickDetector to set
	 */
	public void setClickDetector(ClickDetector clickDetector) {
		this.clickDetector = clickDetector;
	}

	/**
	 * @return the clickDetector
	 */
	public ClickDetector getClickDetector() {
		return clickDetector;
	}


	/**
	 * @return the iCI
	 */
	public double getICI() {
		if (clickDetector.getClickControl().isViewerMode()) {
			int nSuper = getSuperDetectionsCount();
			if (nSuper == 0) {
				return -1;
			}
			return ICI;
		}
		else {
			return ICI;
		}
	}


	/**
	 * @param iCI the iCI to set
	 */
	public void setICI(double iCI) {
		this.ICI = iCI;
	}

	protected void setChannelGroupDetector(ChannelGroupDetector channelGroupDetector) {
		this.channelGroupDetector = channelGroupDetector;
	}


	public ChannelGroupDetector getChannelGroupDetector() {
		return channelGroupDetector;
	}


	/**
	 * @param clickFlags the clickFlags to set
	 */
	public void setClickFlags(int clickFlags) {
		this.clickFlags = clickFlags;
	}


	/**
	 * @return the clickFlags
	 */
	public int getClickFlags() {
		return clickFlags;
	}


	/**
	 * @param tempICI the tempICI to set
	 */
	public void setTempICI(double tempICI) {
		this.tempICI = tempICI;
	}


	/**
	 * @return the tempICI
	 */
	public double getTempICI() {
		return tempICI;
	}


	/**
	 * 
	 * @return the ZeroCrossingStats array (array size=number of channels)
	 */
	public ZeroCrossingStats[] getZeroCrossingStats() {
		// if zerocrossingstats have not been saved yet, create them now
		if (zcs==null) {
			zcs=clickDetector.getClickControl().getClickIdentifier().getZeroCrossingStats(this);
		} 

		// test to see if zcs really contains data.  This is not the case if we're in Viewer mode
		try {
			int test = zcs[0].nCrossings;
		} catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
			createZeroCrossings();
		}
		return zcs;
	}


	/**
	 * This method is copied from createZeroCrossings in SweepClassifierWorker.  But instead
	 * of passing the wav data array and duration, just use the fields available in this class.
	 * This method has been add to calculate the zero crossing data when in Viewer Mode and
	 * the SweepClassifierWorker is not available.
	 * 
	 */
	private void createZeroCrossings() {
		/*
		 * Make an array that must be longer than needed, then 
		 * cut it down to size at the end - will be quicker than continually
		 * growing an array inside a loop. 
		 */

		// call the getWaveData method to reconstruct the wav info if needed.  This can happen
		// if the orig waveData has been cleared in order to free up space
		double[][] wavData = this.getWaveData();
		int nChan = wavData.length;
		int nSamp = wavData[0].length;
		ZeroCrossingStats[] zeroCrossingStats = new ZeroCrossingStats[nChan];
		double exactPos;
		for (int j = 0; j < nChan; j++) {
			double[] zc = new double[nSamp]; // longer than needed. 
			double lastPos = -1;
			int nZC = 0;
			for (int i = 0; i < nSamp-1; i++) {
				if (wavData[j][i] * wavData[j][i+1] > 0) {
					continue; // no zero crossing between these samples
				}
				double divisor = wavData[j][i] - wavData[j][i+1];
				if (divisor!=0) {
					exactPos = i + wavData[j][i] / (divisor);
				} else {
					//System.out.println("Error division by zero i=" + i + ", j=" + j + ", waveData[j][i]=" + waveData[j][i]);
					exactPos = 0;
				}
				/*
				 * Something funny happens if a value is right on zero since the 
				 * same point will get selected twice, so ensure ignore ! 
				 */
				if (exactPos > lastPos) {
					lastPos = zc[nZC++] = exactPos;
				}
			}
			zeroCrossingStats[j] = new ZeroCrossingStats(Arrays.copyOf(zc, nZC), clickDetector.getSampleRate());
		}
		setZeroCrossingStats(zeroCrossingStats);
	}

	/**
	 * 
	 * @param zcs save the ZeroCrossingStats array (array size=number of channels)
	 */
	public void setZeroCrossingStats(ZeroCrossingStats[] zcs) {
		this.zcs = zcs;
	}

	/**
	 * apply a Hanning window to the passed dataset.  Note that the size of the window
	 * is the length of te full dataset that is passed.
	 * 
	 * @param data a double array
	 * @return the windowed data array
	 */
	public static double[] applyHanningWindow(double[] data) {
		double[] windowedData = new double[data.length];
		for (int i = 0; i < data.length; i++)
		{
			windowedData[i] = (double) (data[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (data.length-1))));
		}
		return windowedData;
	}


	/**
	 * @return the offlineEventID
	 */
	public int getOfflineEventID() {
		return offlineEventID;
	}


	/**
	 * @param offlineEventID the offlineEventID to set
	 */
	public void setOfflineEventID(int offlineEventID) {
		this.offlineEventID = offlineEventID;
	}


	/**
	 * @return the clickNumber
	 */
	public long getClickNumber() {
		return clickNumber;
	}


	/**
	 * Get the trigger bitmap, i.e. which channels triggers 
	 * @return the trigger map. 
	 */
	public int getTriggerList() {
		return triggerList;
	}
	
	@Override
	public double getAmplitudeDB() {
		
		/**
		 * Override to allow the click detector to change amplitude in viewer mode without having to move to the next file.
		 */
		if (!Double.isNaN(getCalculatedAmlitudeDB()) && !isForceAmpRecalc()) {
			return getCalculatedAmlitudeDB();
		} 
		else {
			//this should get called very rarely. 
			if (this.getClickDetector().getAquisitionProcess()!=null) {
				setCalculatedAmlitudeDB(this.getClickDetector().getAquisitionProcess().
						rawAmplitude2dB(this.getMeanAmplitude(), PamUtils.getLowestChannel(getChannelBitmap()), false));
			}
			setForceAmpRecalc(false);
			return getCalculatedAmlitudeDB();
		}
	}
	
	/**
	 * Get a spectrogram image of the click. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. 
	 * @param fftSize - the FFT size in samples
	 * @param fftHop - the FFT hop in samples
	 * @param windowType - the window type @see WindowFunction.getWindowFunc(windowFunction, fftLength);
	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClickSpectrogram getClickSpectrogram(int fftSize, int fftHop, int windowType) {
		if (clickSpectrogram==null || clickSpectrogram.getFFTHop()!=fftHop || clickSpectrogram.getFFTSize()!=fftSize) {
			clickSpectrogram = new ClickSpectrogram(this); 
			clickSpectrogram.calcSpectrogram(this.getWaveData(), fftSize, fftHop, 1); 
		}
		return clickSpectrogram;
	}

	/**
	 * Get a spectrogram image of the click. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. 
	 * @param fftSize - the FFT size in samples
	 * @param fftHop - the FFT hop in samples
	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClickSpectrogram getClickSpectrogram(int fftSize, int fftHop) {
		return getClickSpectrogram( fftSize,  fftHop,  1);
	}
	
	
	/**
	 * Get a spectrogram image of the click. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. The the spectrogram is null then it is calculated with a default FFT length of 512 and FFT Hop of 256

	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClickSpectrogram getClickSpectrogram() {
		if (clickSpectrogram==null) {
			clickSpectrogram = new ClickSpectrogram(this); 
			clickSpectrogram.calcSpectrogram(this.getWaveData(), 512, 256, 1); 
		}
		return clickSpectrogram;
	}

	protected static CPUMonitor originMon, headingMon;


	@Override
	public synchronized SnapshotGeometry calcSnapshotGeometry() {
		originMon.start();
		SnapshotGeometry geom = super.calcSnapshotGeometry();
		originMon.stop();
		return geom;
	}


	@Override
	public RawDataTransforms getDataTransforms() {
		return this.rawdataTransforms;
	}

//	@Override
//	public void calcOandAngles() {
//		originMon.start();
//		super.calcOandAngles();
////		SnapshotGeometry geom = ArrayManager.getArrayManager().getSnapshotGeometry(getHydrophoneBitmap(), getTimeMilliseconds());
////		if (geom != null) {
////			setOriginLatLong(geom.getReferenceGPS());
////		}
//		originMon.stop();
//	}
//
//
//	@Override
//	public void calcHeadingandOrigin() {
//		headingMon.start();
//		super.calcHeadingandOrigin();
//		headingMon.stop();
//	}



	

	




}
