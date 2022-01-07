package angleMeasurement;

import java.util.Arrays;

import PamUtils.PamUtils;

import pamMaths.Regressions;

/**
 * Class to sort out angle calibration data. <br>
 * As for the Fluxgate 3030 calibration it's set so that
 * 0 measured will still give 0 after calibration and all
 * other values are relative to that. 
 * <br>
 * Since there may not be an actual calibration point at 0, 
 * the whole calibration table need to be offset so that the
 * first point is at 0. 
 * 
 * @author Douglas Gillespie
 *
 */
public class AngleCalibration {
	
	/**
	 * values measured from compass rose
	 */
	private double[] trueValues; 
	/**
	 * values from angle measuring device.
	 */
	private double[] measuredValues;  
	/**
	 * sorted values measured from compass rose
	 */
	private double[] sortedTrueValues;
	/**
	 * sorted values from angle measuring device.
	 */
	private double[] sortedMeasuredValues;
	
	/**
	 * sorted true values which will give a fit of 0 out for0 in
	 */
	private double[] zeroedSortedTrueValues;
	
	/**
	 * pre prepared second order fits around each group of three points for use in 
	 * calibration calculations
	 */
	private double[][] preparedFits;
	
	private double[] preparedFitOffsets;
	
	/**
	 * true angle for zero measured. 
	 */
	private double zeroTrue;
	
	/**
	 * max error estimated over all angles. 
	 */
	private double maxError;
	
	private Regressions regressions = new Regressions();
	
	private int fitOrder = 2;
	
	private int fitOffset = 1;
	
	private int fitPoints = 3;
	
	
	public AngleCalibration(double[] trueValues, double[] measuredValues) {
		super();
		this.trueValues = trueValues;
		this.measuredValues = measuredValues;
		setFitOrder(fitOrder);
		sortCalibrations();
	}
	/**
	 * calibration values probably came in as an array of 
	 * true points and an array of measuments. We need to 
	 * be able to inverse this. 
	 *
	 */
	private boolean sortCalibrations() {
		// clone then sort !
		sortedMeasuredValues = measuredValues.clone();
		sortedTrueValues = new double[sortedMeasuredValues.length];
		Arrays.sort(sortedMeasuredValues);
		// taht sorted it, but really we need to get the indexes so 
		// we can plot the inverse function !
		int[] sortIndexes = new int[measuredValues.length];
		for (int i = 0; i < sortIndexes.length; i++) {
			sortIndexes[i] = Arrays.binarySearch(sortedMeasuredValues, measuredValues[i]);
			sortedTrueValues[sortIndexes[i]] = trueValues[i];
		}
		if (findZero() == false) {
			return false;
		}
		zeroedSortedTrueValues = sortedTrueValues.clone();
		for (int i = 0; i < zeroedSortedTrueValues.length; i++) {
			zeroedSortedTrueValues[i] -= zeroTrue;
		}
		
		preparedFits = new double[zeroedSortedTrueValues.length][];
		preparedFitOffsets = new double[zeroedSortedTrueValues.length];
		double[] mV;
		double[] tV;
		for (int i = 0; i < zeroedSortedTrueValues.length; i++) {
			mV = extractFitValues(sortedMeasuredValues, i-fitOffset);
			tV = extractFitValues(zeroedSortedTrueValues, i-fitOffset);
			makeSimilar(mV, mV[fitOffset]);
			makeSimilar(tV, mV[fitOffset]);
			preparedFitOffsets[i] = mV[fitOffset];
			for (int j = 0; j < fitPoints; j++) {
				mV[j] -= preparedFitOffsets[i];
			}
			preparedFits[i] = Regressions.polyFit(mV, tV, fitOrder);
		}
		checkMaxError();
		return true;
	}
	
	// use the sortedmeasuredAngles and the zeroed sorted true angles to get
	// an answer...
	public double getCalibratedAngle(double measured) {
		double d1, d2;
		int closestIndex = getClosestIndex(sortedMeasuredValues, measured);
		double[] fit = preparedFits[closestIndex];
		if (fit == null) {
			return Double.NaN;
		}
		measured = makeSimilar(measured, preparedFitOffsets[closestIndex]);
		d1 = Math.abs(measured - sortedMeasuredValues[closestIndex]);
		d1 = makeSimilar(d1, 0);
		double calAngle = Regressions.value(fit, measured - preparedFitOffsets[closestIndex]);
		
		if (true) {
			// try the next nearest too and take a mean.
			int nextNearest;
			double nextNearestAngle = calAngle;
			if (measured > sortedMeasuredValues[closestIndex]) {
				nextNearest = closestIndex + 1;
			}
			else {
				nextNearest = closestIndex - 1;
			}
			if (nextNearest < 0) {
				nextNearest += sortedMeasuredValues.length;
			}
			if (nextNearest >= sortedMeasuredValues.length){
				nextNearest -= sortedMeasuredValues.length;
			}
			fit = preparedFits[nextNearest];
			if (fit != null) {
				measured = makeSimilar(measured, preparedFitOffsets[nextNearest]);
				d2 = Math.abs(measured - sortedMeasuredValues[nextNearest]);
				d2 = makeSimilar(d2, 0);
				nextNearestAngle = Regressions.value(fit, measured - preparedFitOffsets[nextNearest]);
				nextNearestAngle = makeSimilar(nextNearestAngle, calAngle);
				calAngle = (calAngle * d2 + nextNearestAngle * d1) / (d1 + d2);
			}
		}
		
		return PamUtils.constrainedAngle(calAngle);
	}
	
	/**
	 * someone is sure to want to know what the max error is !
	 *
	 */
	private void checkMaxError() {
		maxError = 0;
		double measured, calibrated, correction;
		for (int i = 0; i <= 359; i++) {
			measured = i;
			calibrated = getCalibratedAngle(measured);
			correction = calibrated - measured;
			correction = PamUtils.constrainedAngle(correction, 180);
			maxError = Math.max(maxError, Math.abs(correction));
		}
	}
	
	public int getClosestIndex(double[] searchArray, double searchValue) {
		double[] similarArray = searchArray.clone();
		makeSimilar(similarArray, searchValue);
		double smallestDiff = Math.abs(searchValue - similarArray[0]);
		int closestIndex = 0;
		double diff;
		for (int i = 1; i < similarArray.length; i++) {
			diff = Math.abs(searchValue - similarArray[i]);
			if (diff < smallestDiff) {
				closestIndex = i;
				smallestDiff = diff;
			}
		}
		return closestIndex;
	}
	
	/**
	 * work out which true angle zero measured refers to. 
	 * <br>
	 * uses quad fit, need to work out if zero is closest to 
	 * the 0th or the last value. 
	 */
	private boolean findZero() {
		double[] mV;
		double[] tV;
		int nP = trueValues.length;
		if (360 - sortedMeasuredValues[nP-1] < sortedMeasuredValues[0]) {
			// zero point is closest to the last value in the list, not the first
			mV = extractFitValues(sortedMeasuredValues, nP-fitOffset-1);
			tV = extractFitValues(sortedTrueValues, nP-fitOffset-1);
		}
		else {
			// zero point is closest to the first value in the list. 
			mV = extractFitValues(sortedMeasuredValues, nP-fitOffset);
			tV = extractFitValues(sortedTrueValues, nP-fitOffset);
		}
		makeSimilar(mV, 0);
		makeSimilar(tV, 0);
		double[] fit = Regressions.polyFit(mV, tV, fitOrder);
		if (fit == null) {
			zeroTrue = 0;
			return false;
		}
		zeroTrue = fit[0];
		return true;
	}
	
	private double[] extractFitValues(double[] dataArray, int startIndex) {
		int index;
		double values[] = new double[fitPoints];
		for (int i = 0; i < fitPoints; i++) {
			index = startIndex + i;
			if (index >= dataArray.length) {
				index -= dataArray.length;
			}
			if (index < 0) {
				index += dataArray.length;
			}
			values[i] = dataArray[index];
		}
		return values;
	}
	
	/**
	 * makes angles similar to some target value by adding and subtracting 360.
	 * <br> for instance if the target is 0, then angles will be between -180 and 180
	 * <br> if target is 180, angles will be between 0 and 360, etc. 
	 * 
	 * @param dataArray
	 * @param targetValue
	 */
	private void makeSimilar(double[] dataArray, double targetValue) {
		for (int i = 0; i < dataArray.length; i++) {
			dataArray[i] = makeSimilar(dataArray[i], targetValue);
		}
	}

	private double makeSimilar(double dataValue, double targetValue) {
		double max = targetValue + 180;
		double min = targetValue - 180;
		while (dataValue >= max) {
			dataValue -= 360;
		}
		while (dataValue < min) {
			dataValue += 360;
		}
		
		return dataValue;
	}
	
	private void makeSimilar(double[] dataArray) {
		makeSimilar(dataArray, dataArray[0]);
	}
	
	public double[] getMeasuredValues() {
		return measuredValues;
	}
	public void setMeasuredValues(double[] measuredValues) {
		this.measuredValues = measuredValues;
	}
	public double[] getTrueValues() {
		return trueValues;
	}
	public void setTrueValues(double[] trueValues) {
		this.trueValues = trueValues;
	}
	public double[] getSortedMeasuredValues() {
		return sortedMeasuredValues;
	}
	public double[] getSortedTrueValues() {
		return sortedTrueValues;
	}
	public double getMaxError() {
		return maxError;
	}
	public double getZeroTrue() {
		return zeroTrue;
	}
	public void setFitOrder(int fitOrder) {
		this.fitOrder = fitOrder;
		fitOffset = fitOrder / 2;
		fitPoints = fitOrder + 1;
	}
	
	
}
