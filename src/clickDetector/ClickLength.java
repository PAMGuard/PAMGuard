package clickDetector;

import Filters.SmoothingFilter;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;
import signal.Hilbert;

/**
 * Calculates the click length. This is for generic PamDataUnits which implement RawDataHolder. 
 * <p>
 * Some data units, e.g. a ClickDetection keep a bunch of their filtered, analytic wavefroms etc. in 
 * memory. This makes length calculation much faster. Bespoke length calculation methods are therefore
 * advisable if available. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickLength {
	
	/**
	 * Hilbert object for hilbert transforms. 
	 */
	Hilbert hilbert = new Hilbert();
	
	/**
	 * The current FFT filter. 
	 */
	private FFTFilter fftFilter; 
	
	public ClickLength(){
		
	}
	
	
	public int[][] lengthData(PamDataUnit click, double lengthdB, int peakSmoothing) {
		
		if (click instanceof RawDataHolder) {
			return createLengthData((RawDataHolder) click, click.getParentDataBlock().getSampleRate(),lengthdB, 
					peakSmoothing,  false, null); 
		}
		else {
			System.err.println("clickDetector.ClickLength. The data unit does not implement RawDataHolder");
		}
	
		return null;
	}
	
	/**
	 * Creates a 2D array of length data[channels][start/end]
	 * @param click - the click detection 
	 * @param nChannels - the number of channels to process
	 * @param lengthdB - the dB drop for peak finding
	 * @param lengthSmoothing - the number of bins to smooth waveform for length calculation 
	 * @param enableFFTFilter - true to use filter
	 * @param fftFilterParams - the filter parameters- this is null if no filter is used. 
	 * @return 2d array of length compensated click bin positions to use for modified measurements. 
	 */
	public int[][] createLengthData(RawDataHolder click, float sampleRate, double lengthdB, 
			int lengthSmoothing, boolean enableFFTFilter, FFTFilterParams fftFilterParams) {
		
		int nChannels = click.getWaveData().length; 
		int[][] tempLengthData = new int[nChannels][2];		// 2015/09/13 Rocca serialVersionUID = 22 changed from lengthData to local variable tempLengthData
		double[] aWave;
		double maxVal;
		int maxIndex;
		double threshold;
		double threshRatio = Math.pow(10., Math.abs(lengthdB)/20);
		int waveLen;
		int p;
		
		double[][] analysticWaveforms = getAnalyticWaveforms(click.getWaveData(), sampleRate, fftFilterParams);
		for (int i = 0; i < nChannels; i++) {
			aWave = analysticWaveforms[i]; 
			if (aWave == null) {
				return null;
			}
			aWave = SmoothingFilter.smoothData(aWave, lengthSmoothing);
			waveLen = aWave.length;
			maxVal = aWave[0];
			maxIndex = 0;
			for (int s = 1; s < waveLen; s++) {
				if (aWave[s] > maxVal) {
					maxVal = aWave[s];
					maxIndex = s;
				}
			}
			threshold = maxVal / threshRatio;
			p = maxIndex-1;
//			try {
			tempLengthData[i][0] = 0;
//			} catch (NullPointerException e) {
//				System.out.println("NullPointerException i=" + i);
//				System.out.println("NullPointerException lengthData[0][0]=" + lengthData[0][0]);
//				System.out.println("NullPointerException lengthData[i][0]=" + lengthData[i][0]);
//				System.out.println("waiting point");
//			}
			for (; p >= 0; p--) {
				if (aWave[p] < threshold) {
//					try {
					tempLengthData[i][0] = p+1;
//					} catch (NullPointerException e) {
//						System.out.println("NullPointerException i=" + i);
//						System.out.println("NullPointerException lengthData[0][0]=" + lengthData[0][0]);
//						System.out.println("waiting point");						
//					}
					break;
				}
			}
			p = maxIndex+1;
			try {
				tempLengthData[i][1] = waveLen;
			} catch (NullPointerException e) {
				
			}
			for (; p < waveLen; p++) {
				if (aWave[p] < threshold) {
					tempLengthData[i][1] = p-1;
					break;
				}
			}
		}
		return tempLengthData; 
	}
	
	/**
	 * Get filtered waveform data for all channels. <p>
	 * Data are filtered in the frequency domain using an FFT / Inverse FFT. 
	 * @param filterParams filter parameters
	 * @return array of filtered data
	 */
	public double[][] getFilteredWaveData(double[][] waveform, float sampleRate, FFTFilterParams filterParams) {
		//System.out.println("Make filterred wave data!: " + (filterParams != oldFFTFilterParams));
		if (filterParams==null) {
			return waveform; 
		}
		else {
			return makeFilteredWaveData(waveform, sampleRate, filterParams);
		}
	}

	/**
	 * Get the analystic waveform for a data unit. 
	 * @param i
	 * @param enableFFTFilter
	 * @param fftFilterParams
	 * @return
	 */
	private  double[][] getAnalyticWaveforms(double[][] waveData, float sampleRate,  FFTFilterParams fftFilterParams) {

		double[][] analysticWaveforms = new double[waveData.length][waveData[0].length];
		
		double[][] filteredData = getFilteredWaveData(waveData, sampleRate, fftFilterParams); 
		//		if (analyticWaveform[iChan] == null) {
		
		for (int i=0; i<analysticWaveforms.length; i++) {
			analysticWaveforms[i] =hilbert.
				getHilbert(filteredData[i]);
		}
		return analysticWaveforms;
	}

	
	/**
	 * Filter data using a simple FFT filter
	 * @param waveData - the raw data between -1 and 1 and multi channel
 	 * @param filterParams - the filter parameters.
	 * @param sampleRate - the sample rate in samples per second.
	 * @return
	 */
	private double[][] makeFilteredWaveData(double[][] waveData, float sampleRate, FFTFilterParams filterParams) {
		if (waveData == null || waveData.length == 0) {
			return null;
		}
		
		// now make a zeroed copy of it all. 
		double rotData[][] = new double[waveData.length][waveData[0].length];
		for (int iChan = 0; iChan < waveData.length; iChan++) {
			double[] rotCorr = getRotationCorrection(waveData[iChan]);
			for (int iSamp = 0; iSamp < waveData[0].length; iSamp++) {
				rotData[iChan][iSamp] = waveData[iChan][iSamp] - rotCorr[iSamp];
			}
		}
		int nChan = waveData.length;
		int dataLen = waveData[0].length;
		double[][] filteredWaveData = new double[nChan][dataLen];
		FFTFilter filter = getFFTFilter(filterParams, sampleRate);
		for (int i = 0; i < nChan; i++) {
			filter.runFilter(rotData[i], filteredWaveData[i]);
		}
		return filteredWaveData;
	}
	
	
	/**
	 * Get a correction based on the slope of the waveform which 
	 * can be used to remove large DC / LF offsets in the waveform. 
	 * @param channel - the channel to correct
	 * @return the corrected waveform
	 */
	public double[] getRotationCorrection(double[] chanData) {
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
	 * Get an FFT filter, mainly used to generate filtered waveforms within click detections. 
	 * @param fftFilterParams
	 * @return FFT filter object. 
	 */
	public FFTFilter getFFTFilter(FFTFilterParams fftFilterParams, float sampleRate) {
		if (fftFilter == null) {
			fftFilter = new FFTFilter(fftFilterParams, sampleRate);
		}
		else {
			fftFilter.setParams(fftFilterParams, sampleRate);
		}
		return fftFilter;
	}



}
