package dbht;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class DbHtParameters implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	
	/**
	 * Data source name
	 */
	public String dataSource;
	
	/**
	 * Measurement interval in seconds. 
	 */
	public int measurementInterval = 10;
	
	/**
	 * Channel map for measurements. 
	 */
	public int channelMap;
	
	/**
	 * log2 of the filter order (actual filter will probably be
	 * one less than this value). 
	 */
	public int filterLogOrder = 7;
	
	/**
	 * Gamma for Chebychev window. 
	 */
	public double chebyGamma = 3;
	
	/**
	 * Array of frequency points
	 * Must be same length as hearingThreshold
	 */
	private  double[] frequencyPoints;
	
	public int getNumHtPoints() {
		if (frequencyPoints == null) {
			return 0;
		}
		return frequencyPoints.length;
	}
	
	/**
	 * array of hearing threshold values. 
	 * Must be same length as frequencyPoints
	 */
	public double[] hearingThreshold;
	
	public File lastImportFile;

	transient private double[] filterFrequencies;

	transient private double[] filterGains;
		
	public double[] getFilterFrequencies(double sampleRate) {
		return filterFrequencies;
	}
	
	public double[] getFilterGains(double sampleRate) {
		return filterGains;
	}
	
	public double getLowestThreshold() {
		if (hearingThreshold == null || hearingThreshold.length == 0) {
			return Double.NaN;
		}
		double low = hearingThreshold[0];
		for (int i = 1; i < hearingThreshold.length; i++) {
			low = Math.min(low, hearingThreshold[i]);
		}
		return low;
	}
	
	public synchronized int calculateFilterThings(double sampleRate) throws DbHtException {
		double niquist = sampleRate/2;
		if (frequencyPoints == null) {
			throw new DbHtException("No Frequency points have been defined");
		}
		if (hearingThreshold == null) {
			throw new DbHtException("No Hearing threshold values have been defined");
		}
		if (frequencyPoints.length != hearingThreshold.length) {
			throw new DbHtException("Frequency points and hearign threshold arrays are not the same length");
		}
		int n = frequencyPoints.length;
		if (n == 0) {
			throw new DbHtException("No Frequency points have been defined");
		}
		
		/*
		 * check the frequency values are n increasing order
		 */
		for (int i = 1; i < n; i++) {
			if (frequencyPoints[i] < frequencyPoints[i-1]) {
				throw new DbHtException("Frequency points are not in ascending order");
			}
		}
		
		/**
		 * work out the most sensitive bin.
		 */
		double lowestThresh = Double.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			lowestThresh = Math.min(lowestThresh, hearingThreshold[i]);
		}
		/**
		 * See if we need to pack out values out so that they begin and end in 0
		 */
		if (frequencyPoints[0] != 0) {
			n++;
		}
		if (frequencyPoints[frequencyPoints.length-1] != niquist) {
			n++;
		}
		filterFrequencies = new double[n];
		filterGains = new double[n];
		int iP = 0;
		if (frequencyPoints[0] != 0) {
			filterFrequencies[0] = 0;
			filterGains[0] = 0;
			iP++;
		}
		for (int i = 0; i < frequencyPoints.length; i++, iP++) {
			filterFrequencies[iP] = frequencyPoints[i]/niquist;
			filterGains[iP] = Math.pow(10., (lowestThresh-hearingThreshold[i])/20);
		}
		if (frequencyPoints[frequencyPoints.length-1] != niquist) {
			filterFrequencies[n-1] = 1.0;
			filterGains[n-1] = 0;
		}
		
		
		return n;
	}


	@Override
	public DbHtParameters clone() {
		try {
			DbHtParameters newP =  (DbHtParameters) super.clone();
			// do a full copy of the arrays. 
			if (filterFrequencies != null) {
				newP.filterFrequencies = Arrays.copyOf(filterFrequencies, filterFrequencies.length);
			}
			if (hearingThreshold != null) {
				newP.hearingThreshold = Arrays.copyOf(hearingThreshold, hearingThreshold.length);
			}
			return newP;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the frequencyPoints
	 */
	public synchronized double[] getFrequencyPoints() {
		return frequencyPoints;
	}

	/**
	 * @param frequencyPoints the frequencyPoints to set
	 */
	public synchronized void setFrequencyPoints(double[] frequencyPoints) {
		this.frequencyPoints = frequencyPoints;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("frequencyPoints");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return frequencyPoints;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			PamParameterData param = ps.findParameterData("lastImportFile");
			param.setShortName(lastImportFile.getAbsolutePath());	// add in the actual filename
		}
		catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
