package PamUtils.avrgwaveform;

import Localiser.DelayMeasurementParams;
import Localiser.algorithms.DelayGroup;
import Localiser.algorithms.TimeDelayData;
import Localiser.algorithms.UpSampler;
import PamUtils.PamArrayUtils;
import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

/**
 * Create average waveform and FFT spectrum.   
 * 
 * @author Jamie Macaulay
 *
 */
public class AverageWaveform {
	
	/**
	 * The average waveform. Note that this is stored as the up-sampled waveform. 
	 */
	private double[] avrgWaveform; 
	
	/**
	 * The average FFT spectra. 
	 */
	private double[] avrgSpectra; 

	/**
	 * Used for calculating time delays to average waveforms 
	 */
	private DelayGroup delayGroup = new DelayGroup();
	
	/**
	 * The delay measurement parameters. 
	 */
	private DelayMeasurementParams delayParams = new DelayMeasurementParams(); 
	
	/**
	 * Up samples the waveform
	 */
	private UpSampler upSampler = new UpSampler(delayParams.getUpSample());
	
	/**
	 * Fast FFT to calculate
	 */
	protected FastFFT fastFFT = new FastFFT();

	
	/**
	 * Up sampling to improve accuracy. 
	 */
	private int upSample = 3;

	
	/**
	 * The fft length to use for averaging the spectra. This should be long enough so that 
	 * transients are not missed out. 
	 */
	private int fftLength = 1024;  
	
	/*
	 * The total number of wavefroms that have been included in the average. 
	 */
	int count =0;

	/**
	 * The sample rate. 
	 */
	private float sampleRate; 
	
	public AverageWaveform() {
	}
	
	
	/**
	 * Add a waveform to the  average waveform and spectra. The spectra and waveform are averaged 
	 * separately i.e. the spectra of the average waveform is not necessarily equal to 
	 * the average spectra. This is to prevent constructive/destructive interference in 
	 * averaging waveform from altering spectra results. 
	 * @param waveform - the raw waveform to add the average
	 * @param sR - the sample rate in samples per second
	 */
	public void addWaveform(double[] waveform, float sR) {
		addWaveform(waveform,  sR, this.fftLength, false); 
	}
	
	
	/**
	 * Add a waveform to the  average waveform and spectra. The spectra and waveform are averaged 
	 * separately i.e. the spectra of the average waveform is not necessarily equal to 
	 * the average spectra. This is to prevent constructive/destructive interference in 
	 * averaging waveform from altering spectra results. 
	 * @param waveform - the raw waveform to add the average
	 * @param sR - the sample rate in samples per second
	 * @param normalise - normalise waveforms before averaging.
	 */
	public void addWaveform(double[] waveform, float sR, boolean normalise) {
		addWaveform(waveform,  sR, this.fftLength, normalise); 
	}


	
	/**
	 * Add a waveform to the  average waveform and spectra. The spectra and waveform are averaged 
	 * separately i.e. the spectra of the average waveform is not necessarily equal to 
	 * the average spectra. This is to prevent constructive/destructive interference in 
	 * averaging waveform from altering spectra results. 
	 * @param waveform - the raw waveform to add the average
	 * @param sR - the sample rate in samples per second
	 * @param normalise - normalise waveforms before averaging.
	 * @param maxFFTLen the maximum FFT length.
	 */
	public synchronized void addWaveform(double[] waveform, float sR, int maxFFTLen, boolean normalise) {
		this.fftLength = maxFFTLen; 
		this.sampleRate = sR; 
		if (avrgWaveform==null) {
			avrgWaveform = upSampler.upSample(waveform, upSample); 
			//the FFT length is the length of the first waveform 
			avrgSpectra = calculateFFT(waveform); 
			return; 
		}
		
		//up sample the waveform
		double[] waveformUp = upSampler.upSample(waveform, upSample); 
		double[] newFFT = calculateFFT(waveform); 
		
		double[][] waveforms = new double[][]{avrgWaveform, waveformUp}; 
	
		//the time delay information. 
		 TimeDelayData[]  timedelayInfo = delayGroup.getDelays(waveforms, upSample*sR, delayParams, null);
		 
		 //now get the time delay in samples
		 int tdSmpls = (int) Math.round(timedelayInfo[0].getDelay()*upSample*sR); 
		 
		 int addIndex; 
		 for (int i= 0; i <avrgWaveform.length; i++) {
			//add the waveform together.  
			addIndex= tdSmpls+i;
			if (addIndex>=0 && addIndex<waveformUp.length) {
				avrgWaveform[i]=avrgWaveform[i]+waveformUp[addIndex]; 
			}
		 }
		 
		//System.out.println("avrgSpectra: " + avrgSpectra.length + " newFFT " + newFFT.length);

		 //add the spectra together
		 for (int i= 0; i <avrgSpectra.length; i++) {
			 avrgSpectra[i] = avrgSpectra[i] + newFFT[i]; 
		 }
		 
		 count++;
	}
	
	/**
	 * Add to an average spectra when no raw data is present e.g. CPOD. Here the average spectra is an amplitude weighted historgram 
	 * of the bandwidth of the detection. 
	 * @param minFreq - the minimum frequency in Hz
	 * @param maxFreq - the maximum frequency in Hz
	 * @param amplitude - the amplitude of the detection in dB
	 * @param sampleRate2 - the sample rate in samples per second. 
	 * @param defaultFFTLen - the defaultFFT length. 
	 */
	public void addWaveform(double minFreq, double maxFreq, double amplitude, float sampleRate2, int defaultFFTLen) {
		this.fftLength = defaultFFTLen; 
		
		//System.out.println("Add to averagewaveform: minFreq " + minFreq + " maxFreq: " + maxFreq + " amplitude: " + amplitude + " sR: " + sampleRate2); 

		if (avrgSpectra==null) {
			//the FFT length is the length of the first waveform 
			avrgSpectra = new double[this.fftLength];
			return; 
		}
		
		double amplitudebin = amplitude/(maxFreq-minFreq); 

		double minFreqBin;
		double maxFreqBin;
		for (int i= 0; i <avrgSpectra.length; i++) {
			minFreqBin = (i/(double) avrgSpectra.length)*(sampleRate2/2); 
			maxFreqBin = (i/(double) avrgSpectra.length)*(sampleRate2/2); 

			if (minFreqBin > minFreq && maxFreqBin<=maxFreq) {
				avrgSpectra[i]+=amplitudebin;
			}
		}
		 count++;
	}
	
	/**
	 * Get the average waveform. Not normalised. 
	 * @return the average waveform
	 */
	public double[] getAverageWaveform() {
		return this.avrgWaveform; 
	}
	
	/**
	 * Get the average spectra. Note this is the true average spectra and not just 
	 * the FFT of the average waveform. 
	 * @return the average spectrum. 
	 */
	public double[] getAverageSpectra() {
		//PamArrayUtils.printArray(avrgSpectra);
		return this.avrgSpectra; 
	}
	
	/**
	 * Get the number of waveforms which were used to make the average. . 
	 * @return the number of waveforms in the average. 
	 */
	public int getAverageCount() {
		return this.count; 
	}
	
	/**
	 * Normalise the spectra or waveform.
	 * @param waveform - the waveform or spectra to normalise. 
	 * @return normalise so that maximum is 1. 
	 */
	public double[] normalise(double[] waveform) {
		return PamUtils.PamArrayUtils.normalise(waveform); 
	}
	
	/**
	 * Calculate the FFT of a waveform. 
	 * @param waveform
	 * @return
	 */
	private double[] calculateFFT(double[] rawData) {
		double[] paddedRawData;
		int i, mn;
		
		paddedRawData = new double[fftLength];
		//double[] rotData = getRotationCorrection(channel);
		mn = Math.min(fftLength, rawData.length);
		for (i = 0; i < mn; i++) {
			paddedRawData[i] = rawData[i];//-rotData[i];
		}
		for (i = mn; i < fftLength; i++) {
			paddedRawData[i] = 0;
		}
		ComplexArray complexSpectrum = fastFFT.rfft(paddedRawData, fftLength);

		return complexSpectrum.magsq();
	}
	
	/**
	 * Clear the current data, both the average wavefrom and spectrum. 
	 */
	public void clearAvrgData() {
		avrgWaveform= null; 
		avrgSpectra=null; 
		count=0; 
	}

	/**
	 * Set the average spectrum. This will delete the current average spectrum. 
	 * @param parseAvrgeSpectrum - the average spectrum to set. 
	 */
	public void setAverageSpectra(double[] parseAvrgeSpectrum) {
		avrgSpectra = parseAvrgeSpectrum; 
	}


	/**
	 * Get the sample rate for the average spectra. 
	 * @return the sample rate in samples per second. 
	 */ 
	public float getSampleRate() {
		return sampleRate;
	}
	

	/**
	 * Set the sample rate for the average spectra. 
	 * @param the sample rate in samples per second. 
	 */ 
	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}



	
	
}
