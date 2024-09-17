package clipgenerator;

import java.util.ArrayList;
import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import Spectrogram.WindowFunction;
import fftManager.FastFFT;

/**
 * Creates and stores a spectrogram image for a wave file snippet. Usually used with small
 * snippets of wave data rather than continious raw data. 
 *
 * @author Jamie Macaulay
 *
 */
public class ClipSpectrogram {

	/**
	 * The FFT hop in samples
	 */
	private int fftHop =  256; 

	/**
	 * The FFT hop in samples
	 */
	private int fftLength =  512;
	
	/**
	 * The window type @see
	 */
	private int windowType = 1; 

	/**
	 * Reference to the click detection. 
	 */
	private PamDataUnit clickDetection; 
	
	/**
	 * A fast fourier transform class. 
	 */
	public FastFFT fastFFT = new FastFFT();


	/**
	 * Spectrogram clip for  very long clicks. This is used by displays to
	 * prevent recalculation of multipe FFT. 
	 */
	private ArrayList<double[][]> specclip;
	
	public ClipSpectrogram(PamDataUnit clickDetection) {
		this.clickDetection=clickDetection; 
	}



	/**
	 * Get the spectrogram
	 * @return the spectrogram for each channel
	 */
	public ArrayList<double[][]> getSpectrogram() {
		return specclip;
	}
	
	/**
	 * Get the spectrogram for a given channel
	 * @param iChan - the relative channel number. 
	 * @return the spectrogram for each channel
	 */
	public double[][] getSpectrogram(int ichan) {
		if (specclip==null) return null; 
		return specclip.get(ichan);
	}


	/**
	 * Get the FFT hope in samples. 
	 * @return the FFT hop.
	 */
	public int getFFTHop() {
		return fftHop;
	}

	/**
	 * Get the FFT size in samples. 
	 * @return the FFT size
	 */
	public int getFFTSize() {
		return fftLength;
	}

	/**
	 * Calculate the click spectrum for all channels. 
	 * 
	 * @param waveData - the raw data for each channel
	 */
	public void calcSpectrogram(double[][] waveData, int fftLength, int fftHop, int windowType) {
		
		//System.out.println("CALC CLICK SPECTROGRAM: fftlength: " + fftLength + " ffthop: " + fftHop); 

		
		if (waveData==null) {
			System.err.println("Click Spectrogram: " + " No wave data:?: uid " + clickDetection.getUID()); 
			return;
		}
		
		int wavLen = waveData[0].length; 

		specclip = new ArrayList<double[][]> (); 

		this.fftHop = fftHop; 
		this.fftLength = fftLength; 
		this.windowType = windowType; 

		double[] waveChunk = new double[fftLength]; 
		ComplexArray complexSpectrum;

		int numbins  = (int) Math.ceil(wavLen/(double) fftHop); 
		int n=0; 
		int currentstart = 0 ; 
		double[] windowFunction = WindowFunction.getWindowFunc(windowType, fftLength);

		for (int i=0; i<waveData.length; i++) {

			double[][] powerSpectogram = new double[numbins][];
			
			n=0;
			currentstart=0; 
			while(currentstart<wavLen && n<powerSpectogram.length) {
				
				if (currentstart+fftHop>=wavLen) {
					//last chunk needs to padded with zeros- no need to do this every time
					Arrays.fill(waveChunk, 0); //most efficient way according to stack overflow. 
				}
				
				//copy wave data into an array
				waveChunk= Arrays.copyOfRange(waveData[i], currentstart, Math.min(currentstart+fftLength, wavLen-1));
				
				//System.out.println("Chunk size: " + ( Math.min(currentstart+fftHop, wavLen-1)-currentstart)); 
				//put it through a window function to suppress side lobes#
				//TODO
				
				
				//now need to apply the FFT transfrom 
				for (int w = 0; w < fftLength; w++) {
					if (w<waveChunk.length) {
						waveChunk[w] = (waveChunk[w] * windowFunction[w]);
					}
				}
				
				//PamUtils.MatrixOps.applyMask(waveChunk, windowFunction); 
				//waveChunk = ClickDetection.applyHanningWindow(waveChunk);
				
				//calculate the FFT 
				complexSpectrum = specTransform(waveChunk); 

				//calculate the power spectrum
				powerSpectogram[n] = complexSpectrum.magsq();

				currentstart=currentstart+fftHop; 
				n++; 
			}
			
			if (n!=powerSpectogram.length) {
				System.err.println("Click Spectrogram: Warning: there could be a null in the spectrogram: " + n + " nBins: " +numbins + " wave length: " + wavLen); 
			}
			
//			if (n==0) {
//				System.err.println("N is " + n + "  currentstart " +  currentstart+  " powerSpectogram.length: " + powerSpectogram.length + "clickDetection.getWaveLength(): " + clickDetection.getWaveLength()); 
//			}
			
			specclip.add(powerSpectogram); 
		}
	} 
	

	/**
	 * Apply a transforms to the raw data to convert from time to frequency/phase domain. 
	 * @param wavChunk - the raw  windowed chunk of data (i.e. after hanning window has been applied)
	 * @param len - the len of the transform e.g. FFT length
	 * @return the ComplexArray with transformed data. 
	 */
	public ComplexArray specTransform(double[] waveChunk) {
		//calculate the FFT 
		return fastFFT.rfft(waveChunk, fftLength);
	}


	/**
	 * Get the last used window type. 
	 * @return the window type @see WindowFunction.getWindowFunc(windowType, fftLength);
	 */
	public int getWindowType() {
		return windowType;
	}


}