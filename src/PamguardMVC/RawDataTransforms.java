package PamguardMVC;

import java.util.Arrays;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import clickDetector.ClickDetection;
import clipgenerator.ClipSpectrogram;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;
import fftManager.FastFFT;
import signal.Hilbert;

/**
 * Holds transforms for raw wave data such as spectrum, spectrogram, cepstrum etc. Can be used for any
 * data unit that implements RawDataHolder. 
 * <p>
 * This provides similar functionality to the many functions in ClickDetection without the need for repeating code. 
 * @author Jamie Macaulay 
 *
 */
public class RawDataTransforms {

	/**
	 * The power spectra for all channels 
	 */
	private double[][] powerSpectra;

	/**
	 * the current spectrum length
	 */
	private int currentSpecLen;

	/**
	 * The current complex spectra for multiple channels 
	 */
	private ComplexArray[] complexSpectrum;

	/**
	 * The current clip spectrogram
	 */
	private ClipSpectrogram dlSpectrogram;

	/**
	 * The raw data holder. 
	 */
	private RawDataHolder rawData;

	/**
	 * The PAM data unit. 
	 */
	@SuppressWarnings("rawtypes")
	private PamDataUnit dataUnit;

	/**
	 * Calculates FFT's
	 */
	protected FastFFT fastFFT = new FastFFT();

	/**
	 * Calculates the cepstrum
	 */
	protected FastFFT cepstrumFFT = new FastFFT();

	/**
	 * The analytic waveform
	 */
	private double[][] analyticWaveform;

	/**
	 * The filtered wave data. 
	 */
	private double[][] filteredWaveData;

	/**
	 * The Hilbert transform. 
	 */
	private Hilbert hilbert = new Hilbert();


	/**
	 * The total power spectrum. This is the sum of the power spectrum across different channels. 
	 */
	private double[] totalPowerSpectrum;

	/**
	 * The old filter paramters for filtering wave data. 
	 */
	private FFTFilterParams oldFFTFilterParams;

	/**
	 * The current FFT filter. 
	 */
	private FFTFilter fftFilter;

	/**
	 * The shortest FFT length. 
	 */
	private int shortestFFTLength;


	private Object synchObject;

	/**
	 * Create a RawDataTransforms with a specified synchronisation object. This is mostly the data unit, 
	 * but in some circumstances may need to be a different object to avoid thread lock. 
	 * @param rawDataHolder
	 * @param synchObject
	 */
	public RawDataTransforms(@SuppressWarnings("rawtypes") PamDataUnit rawDataHolder, Object synchObject) {
		this.rawData=(RawDataHolder) rawDataHolder; 
		this.dataUnit  = rawDataHolder; 
		this.synchObject = synchObject;
	}

	/**
	 * Create raw data transforms using the rawDatAholder for synchronisation. 
	 * @param rawDataHolder
	 */
	public RawDataTransforms(@SuppressWarnings("rawtypes") PamDataUnit rawDataHolder) {
		this(rawDataHolder, rawDataHolder);
	}


	/**
	 * Get the power spectrum. 
	 * @return the power spectrum.
	 */
	public double[][] getPowerSpectrum(int fftLength){
		for (int i=0; i<PamUtils.getNumChannels(dataUnit.getChannelBitmap()); i++){
			getPowerSpectrum(i, fftLength); 
		}
		return powerSpectra; 
	}


	/**
	 * Get the shortest FFT length for the number of samples. 
	 * The is the 
	 * @return the shortest FFT length. 
	 */
	public int getShortestFFTLength() {
		if (shortestFFTLength > 0) {
			return shortestFFTLength;
		}
		shortestFFTLength = PamUtils.getMinFftLength(getSampleDuration());
		return shortestFFTLength;                
	}

	/**
	 * Returns the power spectrum for a given channel (square of magnitude of
	 * complex spectrum) between a minimum and maximum sample bin. Note that this
	 * is not saved and so is recalculated every time this function is called.
	 * 
	 * @param channel channel number
	 * @param minBin - the minimum bin
	 * @param maxBin - the maximum bin. 
	 * @param fftLength
	 * @return Power spectrum
	 */
	public double[] getPowerSpectrum(int channel, int minBin, int maxBin, int fftLength) {
		synchronized (synchObject) {

			if (minBin==0 && maxBin>=this.getWaveData(0).length-1) {

				return getPowerSpectrum(channel,  fftLength); 
			}		
			if (fftLength == 0) {
				fftLength = getCurrentSpectrumLength();
			}

			double[] waveformTrim = new double[maxBin-minBin]; 
			
//			System.out.println("minBin: " +minBin + " maxBin: " + maxBin + "  " + Math.min(this.getWaveData(channel).length, waveformTrim.length) + " " + this.getWaveData(channel).length  + "  " + this.getSampleDuration());

			System.arraycopy(this.getWaveData(channel), minBin, waveformTrim, 0, Math.min(this.getWaveData(channel).length-minBin-1, waveformTrim.length));

			ComplexArray cData =  getComplexSpectrumHann(waveformTrim, fftLength); 

			return cData.magsq();
		}
	}

	/**
	 * Returns the power spectrum for a given channel (square of magnitude of
	 * complex spectrum)
	 * 
	 * @param channel channel number
	 * @param fftLength
	 * @return Power spectrum
	 */
	public  double[] getPowerSpectrum(int channel, int fftLength) {
		synchronized (synchObject) {
			if (powerSpectra == null) {
				powerSpectra = new double[PamUtils.getNumChannels(dataUnit.getChannelBitmap())][];
			}
			if (fftLength == 0) {
				fftLength = getCurrentSpectrumLength();
			}

			if (powerSpectra[channel] == null
					|| powerSpectra[channel].length != fftLength / 2) {
				ComplexArray cData = getComplexSpectrumHann(channel, fftLength);
				currentSpecLen = fftLength;
				powerSpectra[channel] = cData.magsq();
				if (powerSpectra==null){
					System.err.println("DLDetection: could not calculate power spectra");
					return null;

				}
				if (powerSpectra[channel].length != fftLength/2) {
					powerSpectra[channel] = Arrays.copyOf(powerSpectra[channel], fftLength/2);
				}
			}
			return powerSpectra[channel];
		}
	}


	/**
	 * Returns the sum of the power spectra for all channels
	 * 
	 * @param fftLength
	 * @return Sum of power spectra
	 */
	public double[] getTotalPowerSpectrum(int fftLength) {
		synchronized (synchObject) {
			if (fftLength == 0) {
				fftLength = getCurrentSpectrumLength();
			}
			if (fftLength == 0) {
				fftLength = PamUtils.getMinFftLength(getSampleDuration());
			}
			double[] ps;
			if (totalPowerSpectrum == null
					|| totalPowerSpectrum.length != fftLength / 2) {
				totalPowerSpectrum = new double[fftLength / 2];
				for (int c = 0; c < PamUtils.getNumChannels(this.dataUnit.getChannelBitmap()); c++) {
					ps = getPowerSpectrum(c, fftLength);
					for (int i = 0; i < fftLength / 2; i++) {
						totalPowerSpectrum[i] += ps[i];
					}
				}
			}
			return totalPowerSpectrum;
		}
	}




	/**
	 * 
	 * Returns the complex spectrum for a given channel using a set FFT length as
	 * getComplexSpectrum, but applies a Hanning window to the raw data first 
	 * 
	 * @param channel - the channel to calculate
	 * @param fftLength - the FFT length to use. 
	 * @return the complex spectrum - the comnplex spectrum of the wave data from the specified channel. 
	 */
	public ComplexArray getComplexSpectrumHann(int channel, int fftLength) {
		synchronized (synchObject) {
			complexSpectrum = new ComplexArray[PamUtils.getNumChannels(dataUnit.getChannelBitmap())];
			if (complexSpectrum[channel] == null
					|| complexSpectrum.length != fftLength / 2) {

				complexSpectrum[channel] =  getComplexSpectrumHann(rawData.getWaveData()[channel], fftLength); 
				currentSpecLen = fftLength;
			}
			return complexSpectrum[channel];
		}
	}



	/**
	 * Get the complex spectrum of a waveform. 
	 * @param waveData - the wave data. 
	 * @param fftLength
	 * @return
	 */
	public static ComplexArray getComplexSpectrumHann(double[] waveData, int fftLength) {
		double[] paddedRawData;
		double[] rawData;
		int i, mn;
		ComplexArray complexSpectrum; 
		paddedRawData = new double[fftLength];
		//messy this Hann window function should be in a utility class. 
		rawData = ClickDetection.applyHanningWindow(waveData);
		mn = Math.min(fftLength, waveData.length);
		for (i = 0; i < mn; i++) {
			paddedRawData[i] = rawData[i];
		}
		for (i = mn; i < fftLength; i++) {
			paddedRawData[i] = 0;
		}

		FastFFT fastFFT = new FastFFT();

		complexSpectrum= fastFFT.rfft(paddedRawData, fftLength);
		return complexSpectrum;		
	}


	/**
	 * Get the spectrum length
	 * @return the spectrogram length. 
	 */
	public int getCurrentSpectrumLength() {
		if (currentSpecLen<=0) {
			currentSpecLen = PamUtils.getMinFftLength(dataUnit.getSampleDuration());
		}
		return currentSpecLen; 
	}



	/**
	 * Get a spectrogram image of the wave clip. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. 
	 * @param fftSize - the FFT size in samples
	 * @param fftHop - the FFT hop in samples
	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClipSpectrogram getSpectrogram(int fftSize, int fftHop) {
		return getSpectrogram( fftSize, fftHop,  1);
	}


	/**
	 * Get a spectrogram image of the wave clip. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. 
	 * @param fftSize - the FFT size in samples
	 * @param fftHop - the FFT hop in samples
	 * @param windowType - the windowType @see WindowFunction.getWindowFunc(fftParams.windowFunction, fftParams.fftHop);
	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClipSpectrogram getSpectrogram(int fftSize, int fftHop, int windowType) {
		if (dlSpectrogram==null || dlSpectrogram.getFFTHop()!=fftHop || dlSpectrogram.getFFTSize()!=fftSize ||  dlSpectrogram.getWindowType()!=windowType) {
			dlSpectrogram = new ClipSpectrogram(dataUnit); 
			dlSpectrogram.calcSpectrogram(rawData.getWaveData(), fftSize, fftHop, windowType); 
		}
		return dlSpectrogram;
	}


	/**
	 * Get the cepstrum for all channels. 
	 * @param ceplen - the cepstrum length. 
	 * @return the cepstrum for all channels. 
	 */
	public double[][] getCepstrum(int ceplen) {
		for (int i=0; i<PamUtils.getNumChannels(dataUnit.getChannelBitmap()); i++){
			getCepstrum(i, ceplen); 
		}
		return powerSpectra; 
	}



	/**
	 * Calculate the cepstrum for a clip channel. 
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
		cepstrumFFT.ifft(spec, fftLength);
		double[] cepstrum = new double[cepLength];
		for (int i = 3; i < cepLength; i++) {
			cepstrum[i] = spec.getReal(i);
		}
		return cepstrum;
	}

	/**
	 * 
	 * Returns the complex spectrum for a given channel using a set FFT length
	 * 
	 * @param channel
	 * @param fftLength
	 * @return the complex spectrum
	 */
	public ComplexArray getComplexSpectrum(int channel, int fftLength) {
		synchronized (synchObject) {
			double[] paddedRawData;
			double[] rawData;
			int i, mn;

			if (complexSpectrum == null) {
				complexSpectrum = new ComplexArray[getNChan()];
			}
			if (complexSpectrum[channel] == null
					|| complexSpectrum.length != fftLength / 2) {
				paddedRawData = new double[fftLength];
				rawData = getWaveData(channel);
				//double[] rotData = getRotationCorrection(channel);
				
				/**
				 *FIXME
				 * 11/07 Changed from getSampleDuration because an error sometimes occurs where the sample duration
				 * is not the same as the wavefom length...not sure why. 
				 */
				//mn = Math.min(fftLength, getSampleDuration().intValue());
				mn = Math.min(fftLength, rawData.length);
//				System.out.println("fftLength: " + rawData.length + " " + getSampleDuration().intValue() + " mn " +mn);
				for (i = 0; i < mn; i++) {
					paddedRawData[i] = rawData[i];//-rotData[i];
				}
				for (i = mn; i < fftLength; i++) {
					paddedRawData[i] = 0;
				}
				complexSpectrum[channel] = fastFFT.rfft(paddedRawData, fftLength);
			}
			return complexSpectrum[channel];
		}
	}


	/**
	 * Get the analytic waveform for  a given channel
	 * @param iChan channel index
	 * @return analytic waveform
	 */
	public double[] getAnalyticWaveform(int iChan) {
		synchronized (synchObject) {
			if (analyticWaveform == null) {
				analyticWaveform = new double[getNChan()][];
			}
			//		if (analyticWaveform[iChan] == null) {
			analyticWaveform[iChan] = hilbert.getHilbert(getWaveData(iChan));
			//		}
			return analyticWaveform[iChan];
		}
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
	public double[] getAnalyticWaveform(int iChan, boolean filtered, FFTFilterParams fftFilterParams) {
		synchronized (synchObject) {
			if (!filtered || fftFilterParams == null) {
				return getAnalyticWaveform(iChan);
			}
			else {
				return getFilteredAnalyticWaveform(fftFilterParams, iChan);
			}
		}
	}

	/**
	 * Get a filtered version of the analytic waveform. In principle, this could be made more efficient
	 * since the calc is done partly in freqeucny domain - so could save a couple of fft's back and forth. 
	 * @param fftFilterParams FFT filter parameters. 
	 * @param iChan channel number
	 * @return envelope of the filtered data. 
	 */

	public double[] getFilteredAnalyticWaveform(FFTFilterParams fftFilterParams, int iChan) {
		synchronized (synchObject) {
			if (analyticWaveform == null) {
				analyticWaveform = new double[getNChan()][];
			}
			//		if (analyticWaveform[iChan] == null) {
			analyticWaveform[iChan] = hilbert.
					getHilbert(getFilteredWaveData(fftFilterParams, iChan));
			//		}
			return analyticWaveform[iChan];
		}
	}

	/**
	 * Get the analytic waveform for all channels 
	 * if filter params = null, then return normal analytic waveform  
	 * @param fftFilterParams
	 * @return analystic waveforms 
	 */
	public double[][] getFilteredAnalyticWaveform(FFTFilterParams fftFilterParams) {
		if (analyticWaveform == null) {
			analyticWaveform = new double[getNChan()][];
		}
		for (int iChan = 0; iChan < getNChan(); iChan++) {
			if (fftFilterParams != null) {
				analyticWaveform[iChan] = hilbert.
						getHilbert(getFilteredWaveData(fftFilterParams, iChan));
			}
			else {
				analyticWaveform[iChan] = getAnalyticWaveform(iChan);
			}
		}
		return analyticWaveform;
	}


	/**
	 * Get filtered waveform data for a single channel. <p>
	 * Data are filtered in the frequency domain using an FFT / Inverse FFT. 
	 * @param filterParams filter parameters
	 * @param channelIndex channel index
	 * @return filtered waveform data
	 */

	public double[] getFilteredWaveData(FFTFilterParams filterParams, int channelIndex) {
		synchronized (synchObject) {
			filteredWaveData = getFilteredWaveData(filterParams);
			return filteredWaveData[channelIndex];
		}
	}

	/**
	 * Get filtered waveform data for all channels. <p>
	 * Data are filtered in the frequency domain using an FFT / Inverse FFT. 
	 * @param filterParams filter parameters
	 * @return array of filtered data
	 */

	public double[][] getFilteredWaveData(FFTFilterParams filterParams) {
		synchronized (synchObject) {
			//System.out.println("Make filterred wave data!: " + (filterParams != oldFFTFilterParams));
			if (filteredWaveData == null || filterParams != oldFFTFilterParams) {
				filteredWaveData = makeFilteredWaveData(filterParams);
			}
			return filteredWaveData;
		}
	}

	private double[][] makeFilteredWaveData(FFTFilterParams filterParams) {
		double[][] waveData = this.rawData.getWaveData();
		if (waveData == null || waveData[0].length == 0) {
			return null;
		}

		// now make a zeroed copy of it all. 
		double rotData[][] = new double[waveData.length][waveData[0].length];
		for (int iChan = 0; iChan < waveData.length; iChan++) {
			double[] rotCorr = getRotationCorrection(iChan);
			for (int iSamp = 0; iSamp < waveData[0].length; iSamp++) {
				rotData[iChan][iSamp] = waveData[iChan][iSamp] - rotCorr[iSamp];
			}
		}
		int nChan = waveData.length;
		int dataLen = waveData[0].length;
		filteredWaveData = new double[nChan][dataLen];
		FFTFilter filter = getFFTFilter(filterParams);
		for (int i = 0; i < nChan; i++) {
			filter.runFilter(rotData[i], filteredWaveData[i]);
		}
		oldFFTFilterParams = filterParams;
		return filteredWaveData;
	}

	//	private FFTFilter getFFTFilter(FFTFilterParams filterParams) {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	/**
	 * Get an FFT filter, mainly used to generate filtered waveforms within click detections. 
	 * @param fftFilterParams
	 * @return FFT filter object. 
	 */
	public FFTFilter getFFTFilter(FFTFilterParams fftFilterParams) {
		if (fftFilter == null) {
			fftFilter = new FFTFilter(fftFilterParams,getSampleRate());
		}
		else {
			fftFilter.setParams(fftFilterParams, getSampleRate());
		}
		return fftFilter;
	}

	/**
	 * Get the sample rate to use for transforms. 
	 * @return the sample rate. 
	 */
	public float getSampleRate() {
		return this.dataUnit.getParentDataBlock().getSampleRate();
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
	 * Get the wave data for the given channel. 
	 * @param channel channel index
	 * @return wave data
	 */
	public double[] getWaveData(int channel) {
		return this.rawData.getWaveData()[channel];
	}

	/**
	 * Get the wave data for the given channel in int16 format. 
	 * @param channel channel index
	 * @return int16 data array. 
	 */
	public short[] getShortWaveData(int channel) {
		double[] dData = getWaveData(channel);
		if (dData == null) {
			return null;
		}
		short[] shortData = new short[dData.length];
		for (int i = 0; i < shortData.length; i++) {
			shortData[i] = (short) (dData[i]*32767);
		}
		return shortData;
	}


	/**
	 * Get the sample duration. 
	 * @return the duration of the data unit in samples. 
	 */
	private Long getSampleDuration() {
		return this.dataUnit.getSampleDuration();
	}

	/**
	 * Get the number of channels. 
	 * @return
	 */
	private int getNChan() {
		return this.rawData.getWaveData().length;
	}

	/**
	 * Get the current power spectra. Can be null if it has not been calculated using
	 * getPowerSpectrum(....). 
	 * @return the current power spectra
	 */
	public double[][] getCurrentPowerSpectra() {
		return powerSpectra;
	}


	/**
	 * Get the current complex spectrum. Can be null if it has not been calculated in other functions. 
	 * @return the current complex spectrum. 
	 */
	public ComplexArray[] getCurrentComplexSpectra() {
		return this.complexSpectrum;
	}

	/**
	 * Set the current complex spectrum. Can be null to free up memory
	 * @return the current complex spectrum. 
	 */
	public void setComplexSpectrum(ComplexArray[] complexSpectrum) {
		this.complexSpectrum=complexSpectrum; 
	}

	/**
	 * Free eup some memory by deleting the filtered wave data, power spectra and analytic waveform. 
	 */
	public void freeMemory() {
		filteredWaveData = null;
		powerSpectra = null;
		analyticWaveform = null;
	}






}
