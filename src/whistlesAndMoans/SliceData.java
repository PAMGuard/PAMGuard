package whistlesAndMoans;

import PamUtils.complex.ComplexArray;
import fftManager.Complex;
import fftManager.FFTDataUnit;

public class SliceData {
	protected int sliceNumber;
	protected int sliceLength;
	protected boolean[] slicePixs;
	protected FFTDataUnit fftDataUnit;
	protected long startSample;
	protected int nPeaks;
	protected int[][] peakInfo;
	private int peakBin;
	private double rmsAmplitude = Double.NaN;
	
	public int getPeakBin() {
		return peakBin;
	}

	/**
	 * Constructor to use when creating new slices during initial detection
	 * @param sliceNumber slice number
	 * @param sliceLength slice length
	 * @param fftDataUnit reference to FFT Data Unit. 
	 */
	protected SliceData(int sliceNumber, int sliceLength, FFTDataUnit fftDataUnit) {
		this.sliceNumber = sliceNumber;
		this.sliceLength = sliceLength;
		slicePixs = new boolean[sliceLength];
		this.fftDataUnit = fftDataUnit;
		startSample = fftDataUnit.getStartSample();
	}
	
	/**
	 * Constructor to use when creating slices from a mother slice during whistle
	 * fragmentation. 
	 * @param oldSlice reference to slice in mother shape
	 * @param newNumber new slice number (may be smaller)
	 * @param peakToSteal peak number to steal (there will only be one of these). 
	 */
	protected SliceData(SliceData oldSlice, int peakToSteal) {
		this.sliceNumber = oldSlice.sliceNumber;
		this.sliceLength = oldSlice.sliceLength;
		this.fftDataUnit = oldSlice.fftDataUnit;
		// probably don't need slicePixs
		this.nPeaks = 1;
		peakInfo = new int[1][];
		peakInfo[0]= oldSlice.peakInfo[peakToSteal];
		peakInfo[0][3] = 0;
	}
	
	/**
	 * Constructor for creating slices offline when read back
	 * from binary files. 
	 * @param sliceNumber slice nukber
	 * @param peakData peak data (n*4)
	 */
	protected SliceData(int sliceNumber, long startSample, int[][] peakInfo) {
		this.sliceNumber = sliceNumber;
		this.startSample = startSample;
		this.peakInfo = peakInfo;
		this.nPeaks = peakInfo.length;
	}
	
	/**
	 * Repacks the data and returns the lower and upper bounds. 
	 * <p> 0th = lower bound
	 * <p> 1st = peak value
	 * <p> 2nd = upper bound
	 * <p> 3rd = peak number in previous slice (filled in later but allocated now)
	 * @return bounds
	 */
	protected int[] condenseInfo(SliceData prevSlice) {
		boolean on = false;
		nPeaks = countRegions();
		peakInfo = new int[nPeaks][4];
		int iR = 0;
		double maxVal=0, temp;
		double peakMaxVal = 0;
		int maxIndex=0;
		int[] range = {Integer.MAX_VALUE, 0};
		ComplexArray fftData = fftDataUnit.getFftData();
		/*
		 * Note that it's getting the peak within each bit and the 
		 * overall peak for the whole region too. 
		 */
		if (sliceNumber >= 2405) {
			iR = 0;
		}
		for (int i = 0; i < sliceLength; i++) {
			if (slicePixs[i] == false) {
				continue;
			}
			temp = fftData.magsq(i);
			if (temp > peakMaxVal) {
				peakMaxVal = temp;
				peakBin = i;
			}
			if (slicePixs[i] && !on) {
				if (iR >= peakInfo.length) {
					System.out.println("Error in slice peaks");
				}
				peakInfo[iR][0] = i;
				maxVal = temp;
				maxIndex = i;
				on = true;
				range[0] = Math.min(range[0], i);
			}
			else {
				if (temp > maxVal) {
					maxVal = temp;
					maxIndex = i;
				}
			}
			if (on & (i == sliceLength-1 || !slicePixs[i+1])) {
				peakInfo[iR][2] = i;
				peakInfo[iR][1] = maxIndex;
				peakInfo[iR][3] = findOverlappingPeak(peakInfo[iR], prevSlice);
				on = false;
				range[1] = Math.max(range[1], i);
				iR++;
			}
		}
		return range;
	}
	
	public double getRmsAmplitude() {
		if (Double.isNaN(rmsAmplitude)) {
			setRmsAmplitude();
		}
		return rmsAmplitude;
	}

	public void setRmsAmplitude() {
		this.rmsAmplitude = getTotalRMSAmplitude();
	}

	/**
	 * work out the total rms amplitude in all 
	 * set peaks within the slice
	 * Must be called after condenseInfo
	 */
	protected double getTotalRMSAmplitude() {
		if (peakInfo == null) {
			return 0;
		}
		double totalRMS = 0;
		ComplexArray fftData = fftDataUnit.getFftData();
		for (int iP = 0; iP < nPeaks; iP++) {
			for (int iB = peakInfo[iP][0]; iB <= peakInfo[iP][2]; iB++) {
				totalRMS += fftData.magsq(iB);
			}
		}
		totalRMS *= 4; // negative frequencies
		totalRMS /= (fftData.length()*2); // average over fft block length and parseval
		return totalRMS;
	}
	
	private int findOverlappingPeak(int[] peakInfo, SliceData otherSlice) {
		if (otherSlice == null) {
			return -1;
		}
		int nP = otherSlice.nPeaks;
		int[] otherPeak;
		for (int i = 0; i < nP; i++) {
			otherPeak = otherSlice.peakInfo[i];
			if (otherPeak[0] > peakInfo[2] || otherPeak[2] < peakInfo[0]) {
				continue;
			}
			return i;
		}
		return -1;
	}
	private int countRegions() {
		int nR = 0;
		if (slicePixs == null) {
			return nPeaks; // this happens offline !
		}
		/*
		 * Allow for possibility of peak starting in first slice. 
		 */
		if (slicePixs[0]) {
			nR = 1;
		}
		/**
		 * then look for starts - going from off to on. No need to find final off. 
		 */
		for (int i = 1; i < sliceLength; i++) {
			if (slicePixs[i] && !slicePixs[i-1]) {
				nR++;
			}
		}
		return nR;
	}

	public long getStartSample() {
		return startSample;
	}

	
	public int getSliceNumber() {
		return sliceNumber;
	}

	public int getSliceLength() {
		return sliceLength;
	}
	
	public int getnPeaks() {
		return nPeaks;
	}
	
	/**
	 * Peak info is an n x 3 array of bin numbers for 
	 * an FFT slice. Normally in a whistle, n=1, but there may be multiple 
	 * peaks in a branched whistle in which case n will be higher. the 3 numbers
	 * for each peak are the low, peak and high bins of the contour.  
	 * @return peak  information. 
	 */
	public int[][] getPeakInfo() {
		return peakInfo;
	}


	/**
	 * @return the fftDataUnit associated with this slice
	 */
	public FFTDataUnit getFftDataUnit() {
		return fftDataUnit;
	}
	
	/**
	 * Get a two element array of useful bin ranges following the convention 
	 * used in the FFTDataUnit.getUsefulBinRange() whereby the upper limit is 
	 * one greater than the actual used bin.  
	 * @return bin range for future calculations
	 * @see FFTDataUnit.getUsefulBinRange
	 */
	public int[] getUsefulBinRange() {
		if (nPeaks == 0) {
			return null;
		}
		int[] range = {peakInfo[0][0], peakInfo[0][2]+1};
		for (int i = 1; i < nPeaks; i++) {
			range[1] = Math.max(range[1], peakInfo[i][2]+1);
		}
		return range;
	}
	
	
}

