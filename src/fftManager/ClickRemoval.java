package fftManager;

import pamMaths.STD;

/**
 * Function to remove clicks from blocks of data. Can operate 
 * on an existing array or create a new one depending on which 
 * function is called. 
 * 
 * @author Doug Gillespie
 *
 */
public class ClickRemoval {

	private STD std = new STD(); 
	
	private double[] weights = null;
	
	public static final double defaultClickThreshold = 5;
	
	public static final int defaultClickPower = 6;
	
	/**
	 * Writes over existing data. 
	 * @param waveData
	 */
	public void removeClickInPlace(double[] waveData, double threshold, double power) {
		removeClicks(waveData, waveData, threshold, power);
	}
	
	/**
	 * Leaves existing data alone and creates new array.
	 * @param waveData
	 * @return new array of wave data
	 */
	public double[] removeClicks(double[] waveData, double threshold, double power) {
		double[] newData = new double[waveData.length];
		return removeClicks(waveData, newData, threshold, power);
	}
	
	/**
	 * Perform click removal. 
	 * @param sourceData input data array
	 * @param newData output data array
	 * @param threshold threshold for removal (5 is a good value)
	 * @param power removal power (6 is a good value)
	 */
	public double[] removeClicks(double[] sourceData, double[] newData, 
			double threshold, double power) {
		if (newData == null || newData.length != sourceData.length) {
			newData = new double[sourceData.length];
		}
		if (weights == null || weights.length != sourceData.length) {
			weights = new double[sourceData.length];
		}
		double thresh = threshold * std.getSTD(sourceData);
		/**
		 * Need to protect against thresh == 0. 
		 */
		if (thresh <= 0.) {
			for (int i = 0; i < sourceData.length; i++) {
				newData[i] = sourceData[i];
			}
		}
		else {
			double mean = std.getMean();
			for (int i = 0; i < sourceData.length; i++) {
				weights[i] = 1. / (1. + Math.pow((sourceData[i]-mean)/thresh, power));
				newData[i] = sourceData[i] * weights[i];
			}
		}
		return newData;
	}
}
