package PamguardMVC.background;

import PamguardMVC.DataUnitBaseData;

public class SpecBackgroundDataUnit extends BackgroundDataUnit {

	private int loBin;
	private int hiBin;
	private double[] data;

	public SpecBackgroundDataUnit(long timeMilliseconds, long startSample, int channelBitmap, double durationMillis, int loBin, int hiBin, double[] data) {
		super(timeMilliseconds, channelBitmap, durationMillis);
		setStartSample(startSample);
		this.loBin = loBin;
		this.hiBin = hiBin;
		this.data = data;
	}

	public SpecBackgroundDataUnit(DataUnitBaseData basicData, int loBin, double[] data) {
		super(basicData);
		this.loBin = loBin;
		this.hiBin = loBin + data.length;
		this.data = data;
	}

	@Override
	public double getCountSPL() {
		double tot = 0;
		for (int i = 0; i < data.length; i++) {
			tot += Math.pow(data[i], 2);
		}
		return Math.sqrt(tot);
	}	
	
	/**
	 * sum up the SPL within a range of bins
	 * @param minFBin Max bin (correct index for full spectrogram)
	 * @param maxFBin
	 * @return
	 */
	public double getCountSPL(int minFBin, int maxFBin) {
		minFBin -= loBin; // allow for data offset
		maxFBin -= loBin;
		minFBin = Math.max(0, minFBin); // check limits
		maxFBin = Math.min(maxFBin, data.length-1);
		double tot = 0;
		for (int i = minFBin; i <= maxFBin; i++) {
			tot += Math.pow(data[i], 2);
		}
		return Math.sqrt(tot);
	}


	/**
	 * @return the loBin
	 */
	public int getLoBin() {
		return loBin;
	}

	/**
	 * @param loBin the loBin to set
	 */
	public void setLoBin(int loBin) {
		this.loBin = loBin;
	}

	/**
	 * @return the hiBin
	 */
	public int getHiBin() {
		return hiBin;
	}

	/**
	 * @param hiBin the hiBin to set
	 */
	public void setHiBin(int hiBin) {
		this.hiBin = hiBin;
	}

	/**
	 * @return the data
	 */
	public double[] getData() {
		return data;
	}
	
	/**
	 * Set all the data
	 * @param loBin
	 * @param hiBin
	 * @param data
	 */
	public void setData(int loBin, int hiBin, double[] data) {
		setLoBin(loBin);
		setHiBin(hiBin);
		setData(data);
	}

	/**
	 * @param data the data to set
	 */
	public void setData(double[] data) {
		this.data = data;
	}


}
