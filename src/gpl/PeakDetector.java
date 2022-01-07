package gpl;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

/**
 * Really simple detector, looking for peaks rising above some
 * threshold. 
 * @author Doug Gillespie
 *
 */
public class PeakDetector {

	private int minGap;
	private int state = 0;
	private int offCount = 0;
	private DetectedPeak peak;
	private GPLControlledUnit gplControlledUnit;
	private int binLo;
	private int binHi;
	
	public PeakDetector(GPLControlledUnit gplControlledUnit, int minGap, int binLo, int binHi) {
		super();
		this.gplControlledUnit = gplControlledUnit;
		this.minGap = minGap;
		this.binLo = binLo;
		this.binHi = binHi;
	}
	
	/**
	 * Look for peaks in the detection statistic. 
	 * @param fftDataUnit
	 * @param whiteData
	 * @param data
	 * @param lowThreshold
	 * @param highThreshold
	 * @return mostly null, a non null Peak when one is finished & complete. 
	 */
	public DetectedPeak detectPeaks(FFTDataUnit fftDataUnit, double[] whiteData, double data, double lowThreshold, double highThreshold) {
		DetectedPeak returnPeak = null;
		if (state == 0 && data > lowThreshold) {
			// nothing was happening and we're now above the low threshold. 
			peak = new DetectedPeak(fftDataUnit, data, whiteData, getPeak(whiteData));
			state = (data > highThreshold ? 2 : 1);
			offCount = 0;
		}
		else if (state > 0) {
			if (data > lowThreshold) {
				/*
				 * Continue building the peak. 
				 */
				peak.addPoint(fftDataUnit, data, whiteData, getPeak(whiteData));
				if (data > highThreshold) {
					state = 2;
				}
			}
			else {
				/*
				 *  It's dropped below threshold, 
				 *  if it never peaked above highThreshold, then 
				 *  reset detector now. 
				 *  Otherwise see if it's been down long enough to 
				 *  say that it's definitely ended. 
				 */
				if (state == 1) {
					state = 0; // reset, so next call will look for a new peak. 
					peak = null;
				}
				else if (offCount++ > minGap) {
					returnPeak = peak;
					peak = null;
					state = 0;
				}
				else {
					/*
					 * Passes by here if it's below threshold, but hasn't been 
					 * below threshold for long enough to become an isolated peak. 
					 */
					peak.addDeadPoint(whiteData, fftDataUnit);
				}
			}
		}
		
		return returnPeak;
	}
	
	/**
	 * Get the energy within frequency limits for a slice being added to a peak
	 * @param fftDataUnit
	 * @return sum magsq of FFT data. 
	 */
	public double getSliceEnergy(FFTDataUnit fftDataUnit) {
		ComplexArray dat = fftDataUnit.getFftData();
		return dat.sumSquared(binLo, binHi);
	}
	
	/**
	 * Get the peak bin in the white fft data
	 * @param slice
	 * @return peak pos (offset from start of bin_lo, not 0 !
	 */
	private int getPeak(double[] slice) {
		int maxBin = 0;
		double maxVal = slice[0];
		for (int i = 0; i < slice.length; i++) {
			if (slice[i] > maxVal) {
				maxVal = slice[i];
				maxBin = i;
			}
		}
		return maxBin;
	}

	public int getPeakState() {
		return state;
	}
	
}
