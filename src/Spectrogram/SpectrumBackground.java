package Spectrogram;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * Simple background measure for a channel of complex or real FFT data
 * Uses a dead simple decaying mean measurement
 * @author dg50
 *
 */
public class SpectrumBackground {

	private FFTDataBlock parentFFTBlock;
	private int iChannel;
	private int nRunin, nDone;
	private double alpha1;
	private double[] data;

	/**
	 * 
	 * @param parentFFTBlock
	 * @param iChannel
	 */
	public SpectrumBackground(FFTDataBlock parentFFTBlock, int iChannel) {
		this.parentFFTBlock = parentFFTBlock;
		this.iChannel = iChannel;
	}

	/**
	 * Prepare the background measurement, passing a time constant in seconds
	 * @param timeConstSecs time constant in seconds
	 * @return true if OK
	 */
	public boolean prepareS(double timeConstSecs) {
		if (parentFFTBlock == null) {
			return false;
		}
		double dT = parentFFTBlock.getFftHop() / parentFFTBlock.getSampleRate();
		nRunin = (int) (timeConstSecs / dT);
		alpha1 = dT/timeConstSecs; // something small
		nDone = 0;
		synchronized (this) {
			data = new double[parentFFTBlock.getFftLength()/2];		
		}
		return true;
	}
	
	/**
	 * Add a FFT data unit (assume already of the right channel)
	 * @param fftData FFT Complex Data must be of length fftlength / 2
	 * @return current background measure
	 */
	public double[] process(FFTDataUnit fftDataUnit) {
		return process(fftDataUnit.getFftData());
	}

	/**
	 * Add an array of complex FFT data (assume already of the right channel)
	 * @param fftData FFT Complex Data must be of length fftlength / 2
	 * @return current background measure
	 */
	public double[] process(ComplexArray fftData) {
		return process(fftData.magsq());
	}

	/** Add an array of mag squared (power) data (assume already of the right channel)
	 * @param magsq  Data must be of length fftlength / 2
	 * @return current background measure
	 */
	private double[] process(double[] magsq) {
		nDone++;
		double a1 = alpha1;
		if (nDone < nRunin) {
			a1 = 1./nDone;
		}
		double a2 = 1-a1;
		synchronized (this) {
			for (int i = 0; i < data.length; i++) {
				if (!Double.isFinite(magsq[i])) { //  make sure a NaN or an infinite value doesn't creep in
					continue;
				}
				data[i] *= a2;
				data[i] += a1*magsq[i];
			}
		}
		return data;
	}

//	/**
//	 * Get the current background
//	 * @return array of background measures (magnitude squared, no corrections for FFT length, neg frequencies, etc.) 
//	 */
//	private double[] getBackground() {
//		synchronized (this) {
//			return data;
//		}
//	}
	
	/**
	 * Get a copy of the current background. This is often preferable to calling
	 * getBackground() since that other function returns a copy to an array which is 
	 * constantly being updated. So if you're going to manipulate data in that array, then a) it 
	 * may be out of date by the time you use it and b) you'll mess up the internal background measurement
	 * @return array of background measures (magnitude squared, no corrections for FFT length, neg frequencies, etc.) 
	 */
	public double[] copyBackground() {
		synchronized (this) {
			return data.clone();
		}
	}
	

	/**
	 * @return the parentFFTBlock
	 */
	public FFTDataBlock getParentFFTBlock() {
		return parentFFTBlock;
	}

	/**
	 * @return the iChannel
	 */
	public int getiChannel() {
		return iChannel;
	}

	/**
	 * @return the nDone
	 */
	public int getnDone() {
		return nDone;
	}

}
