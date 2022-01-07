package clipgenerator.localisation;

import Localiser.algorithms.Correlations;
import fftManager.Complex;
import fftManager.FastFFT;

/**
 * Class with some functions for calculating cross correlations 
 * between lumps of spectrogram. 
 * @author Doug Gillespie
 *
 */
public class SpectrogramCorrelator {

	protected Correlations correlations = new Correlations();

	private FastFFT fastFFT = new FastFFT();

	public SpectrogramCorrelator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Find the maxima in the cross correlation function from 
	 * two lumps of spectrogram data. Note that the spectrogram 
	 * data will only go to half the original FFT length. 
	 * @param specData1 block of complex spectrogram data
	 * @param specData2 second block of complex spectrogram data
	 * @return cross correlation position and value. 
	 */
	public double[] getDelay(Complex[][] specData1, Complex[][] specData2) {
		if (specData1 == null || specData1.length == 0) {
			return null;
		}
		return getDelay(specData1, specData2, specData1[0].length);
	}
	/**
	 * Find the maxima in the cross correlation function from 
	 * two lumps of spectrogram data. Note that the spectrogram 
	 * data will only go to half the original FFT length. 
	 * @param specData1 block of complex spectrogram data
	 * @param specData2 second block of complex spectrogram data
	 * @param max possible delay maximum possible delay between channels. 
	 * @return cross correlation position and value. 
	 */
	public double[] getDelay(Complex[][] specData1, Complex[][] specData2, double maxDelay) {
		if (specData1 == null || specData1[0] == null) {
			return null;
		}
		int[] fBins = {0, specData1[0].length};
		return getDelay(specData1, specData2, maxDelay, fBins);
	}
	/**
	 * Find the maxima in the cross correlation function from 
	 * two lumps of spectrogram data. Note that the spectrogram 
	 * data will only go to half the original FFT length. 
	 * @param specData1 block of complex spectrogram data
	 * @param specData2 second block of complex spectrogram data
	 * @param analBins frequency bin range for the analysis
	 * @param max possible delay maximum possible delay between channels. 
	 * @return cross correlation position and value. 
	 */
	public double[] getDelay(Complex[][] specData1, Complex[][] specData2, double maxDelay, int[] analBins) {

		if (specData1 == null || specData2 == null) {
			return null;
		}
		if (specData1.length != specData2.length) {
			return null;
		}
		if (specData1.length == 0 || specData1[0].length != specData2[0].length) {
			return null;
		}

		int nSlice = specData1.length;
		int halfFFT = specData1[0].length;
		int fftLength = halfFFT * 2;
		
		if (analBins == null) {
			int[] ab = {0, halfFFT};
			analBins = ab;
		}

		Complex[] conData = Complex.allocateComplexArray(halfFFT*2);
		Complex c1, c2;
		double scale1 = 0, scale2 = 0;
		// (a + ib)*(c-id) = a*c*b*d + i(b*c-a*d)
		for (int iSlice = 0; iSlice < nSlice; iSlice++) {
			//			for (int iSlice = nSlice/2; iSlice < nSlice; iSlice++) {
			scale1 = scale2 = 0;
			for (int i = 0; i < halfFFT; i++) {
				c1 = specData1[iSlice][i];
				c2 = specData2[iSlice][i];
				scale1 += c1.magsq();
				scale2 += c2.magsq();
			}
			scale1 = Math.sqrt(scale1*scale2)*2;
			for (int i = analBins[0]; i < analBins[1]; i++) {
				c1 = specData1[iSlice][i];
				c2 = specData2[iSlice][i];
				conData[i].real += (c1.real*c2.real + c1.imag*c2.imag)/scale1;
				conData[i].imag += (c1.imag*c2.real - c1.real*c2.imag)/scale1;
						//				realData2[iSlice][i] = c1.magsq();
			}
			//			break;
		}
		// now fill in the back half of the fft data prior to taking the inverse fft
		conData[0].assign(0,0);
		for (int i = 0; i < halfFFT-1; i++) {
			conData[fftLength-i-1].real = conData[i].real;
			conData[fftLength-i-1].imag = -conData[i].imag;
		}
		// now take the ifft to get the cross correlation function. 
		int m = FastFFT.log2(fftLength);
		fastFFT.ifft(conData, m);
		
		double xCorrResult[] = correlations.getInterpolatedPeak(conData, 1, maxDelay);
		
		return xCorrResult;
//
//		double[] crossX = new double[fftLength];
//		for (int i = 0; i < halfFFT; i++) {
//			crossX[i+halfFFT] = conData[i].real;
//			crossX[i] = conData[i+halfFFT].real;
//		}
//
//		return crossX;
	}

}
