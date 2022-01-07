package fftManager;

import PamUtils.complex.ComplexArray;

/**
 * Class to better handle Complex FFT Data, including the FFT length along with the
 * Complex data - this is important since FFT length cannot be extracted from the 
 * length of the Complex array since it's sometimes only stored half the fft length 
 * not the entire thing. 
 * @author dg50
 *
 */
public class FFTDataArray {

	private ComplexArray fftData;
	
	private int fftLength;
	
	private int windowFunction;

	/**
	 * Construct an FFT Data Array
	 * @param fftData Array of complex objects
	 * @param fftLength Length of FFT data
	 * @param windowFunction window function used in calculation
	 */
	public FFTDataArray(ComplexArray fftData, int fftLength, int windowFunction) {
		super();
		this.fftData = fftData;
		this.fftLength = fftLength;
		this.windowFunction = windowFunction;
	}
	
	public boolean isFullArray() {
		return (fftData != null && fftData.length() == fftLength);
	}

	public boolean isHalfArray() {
		return (fftData != null && fftData.length() == fftLength/2);
	}

	/**
	 * @return the fftData
	 */
	public ComplexArray getFftData() {
		return fftData;
	}

	/**
	 * @return the fftLength
	 */
	public int getFftLength() {
		return fftLength;
	}

	/**
	 * @return the windowFunction
	 */
	public int getWindowFunction() {
		return windowFunction;
	}
}
