package gpl;

import java.util.ArrayList;

import fftManager.FFTDataUnit;

public class DetectedPeak {

	private ArrayList<double[]> wDataList;
	private ArrayList<FFTDataUnit> fftDataUnits;
	private int startBin, endBin;
	private double maxValue;
	private long startMillis, endMillis;
	private int minFBin, maxFBin;
	private int nDead;
	
	public DetectedPeak(FFTDataUnit fftDataUnit, double data, double[] wData, int peakBin) {
		startBin = endBin = fftDataUnit.getFftSlice();
		startMillis = endMillis = fftDataUnit.getTimeMilliseconds();
		minFBin = maxFBin = peakBin;
		this.maxValue = data;
		this.wDataList = new ArrayList<>();
		wDataList.add(wData);
		fftDataUnits = new ArrayList<>();
		fftDataUnits.add(fftDataUnit);
		nDead = 0;
	}
	
	/**
	 * Add a point that was above threshold. 
	 * @param fftSlice
	 * @param timeMilliseconds
	 * @param data
	 * @param wData
	 * @param peakBin
	 */
	public void addPoint(FFTDataUnit fftDataUnit, double data, double[] wData, int peakBin) {
		endBin = fftDataUnit.getFftSlice();
		endMillis = fftDataUnit.getTimeMilliseconds();
		maxValue = Math.max(maxValue, data);
		wDataList.add(wData);
		fftDataUnits.add(fftDataUnit);
		minFBin = Math.min(minFBin, peakBin);
		maxFBin = Math.max(maxFBin, peakBin);
		nDead = 0;
	}
	
	/**
	 * Add a point that was below threshold. 
	 * Need to keep adding whitened data even if it's not above threshold
	 * @param wData
	 */
	public void addDeadPoint(double[] wData, FFTDataUnit fftDataUnit) {
		nDead++;
		wDataList.add(wData);
		fftDataUnits.add(fftDataUnit);
	}
	
	/**
	 * Called when peak is completed to remove any whitened data that were added 
	 * right at the end
	 */
	public void removeDead() {
		for (int i = 0; i < nDead; i++) {
			wDataList.remove(wDataList.size()-1);
			fftDataUnits.remove(fftDataUnits.size()-1);
		}
//		Double[][] data = (Double[][]) wDataList.toArray();
	}

	/**
	 * Get the start sample, which is the start sample of the first
	 * FFT in the peak NOT a count of FFt's which is wrong when 
	 * starts of files are skipped in Acquisition 
	 * @return first sample of first FFT
	 */
	public long getStartSample() {
		if (fftDataUnits.size() == 0) {
			return 0;
		}
		return fftDataUnits.get(0).getStartSample();
	}	
	/**
	 * Get the end sample, which is the start sample of the first
	 * FFT in the peak NOT a count of FFt's which is wrong when 
	 * starts of files are skipped in Acquisition 
	 * @return last sample of last FFT
	 */
	public long getEndSample() {
		int n = fftDataUnits.size();
		if (n == 0) {
			return 0;
		}
		return fftDataUnits.get(n-1).getLastSample();
	}
	
	/**
	 * @return the wDataList
	 */
	public ArrayList<double[]> getwDataList() {
		return wDataList;
	}
	
	/**
	 * @return List of FFT Data units in the peak
	 */
	public ArrayList<FFTDataUnit> getFFTList() {
		return fftDataUnits;
	}

	/**
	 * @return the startBin
	 */
	public int getStartBin() {
		return startBin;
	}

	/**
	 * @return the endBin
	 */
	public int getEndBin() {
		return endBin;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @return the minFBin
	 */
	public int getMinFBin() {
		return minFBin;
	}

	/**
	 * @return the maxFBin
	 */
	public int getMaxFBin() {
		return maxFBin;
	}
	
	
}
