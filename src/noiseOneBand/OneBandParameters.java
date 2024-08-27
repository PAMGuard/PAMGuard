package noiseOneBand;

import java.io.Serializable;
import java.lang.reflect.Field;

import Filters.FilterParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class OneBandParameters implements Serializable, Cloneable, ManagedParameters {

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
	 * Interval for SEL integration. 
	 */
	public int selIntegrationTime = 120;
	
	/**
	 * Channel map for measurements. 
	 */
	public int channelMap;
	
//	/**
//	 * log2 of the filter order (actual filter will probably be
//	 * one less than this value). 
//	 */
//	private int filterLogOrder = 7;
//	
//	/**
//	 * Gamma for Chebychev window. 
//	 */
//	private double chebyGamma = 3;
	
//	/**
//	 * Array of frequency points
//	 * Must be same length as hearingThreshold
//	 */
//	@Deprecated
//	private  double[] frequencyPoints;

	private FilterParams filterParams = new FilterParams();
	
	public boolean detectPulses = false;
	
	public double singlePulseThreshold = 10;
	
	public double maxPulseLength = 10;	
	
	/**
	 * array of hearing threshold values. 
	 * Must be same length as frequencyPoints
	 */
//	@Deprecated
//	private double[] hearingThreshold;
//	
//	@Deprecated
//	private File lastImportFile;

//	@Deprecated
//	transient private double[] filterFrequencies;
//
//	@Deprecated
//	transient private double[] filterGains;
		
//	public double[] getFilterFrequencies(double sampleRate) {
//		return filterFrequencies;
//	}
	

//	public int getNumHtPoints() {
//		if (frequencyPoints == null) {
//			return 0;
//		}
//		return frequencyPoints.length;
//	}
//	
//	public double[] getFilterGains(double sampleRate) {
//		return filterGains;
//	}
	
//	public double getLowestThreshold() {
//		if (hearingThreshold == null || hearingThreshold.length == 0) {
//			return Double.NaN;
//		}
//		double low = hearingThreshold[0];
//		for (int i = 1; i < hearingThreshold.length; i++) {
//			low = Math.min(low, hearingThreshold[i]);
//		}
//		return low;
//	}
	
//	public synchronized int calculateFilterThings(double sampleRate) throws DbHtException {
//		double niquist = sampleRate/2;
//		if (frequencyPoints == null) {
//			throw new DbHtException("No Frequency points have been defined");
//		}
//		if (hearingThreshold == null) {
//			throw new DbHtException("No Hearing threshold values have been defined");
//		}
//		if (frequencyPoints.length != hearingThreshold.length) {
//			throw new DbHtException("Frequency points and hearign threshold arrays are not the same length");
//		}
//		int n = frequencyPoints.length;
//		if (n == 0) {
//			throw new DbHtException("No Frequency points have been defined");
//		}
//		
//		/*
//		 * check the frequency values are n increasing order
//		 */
//		for (int i = 1; i < n; i++) {
//			if (frequencyPoints[i] < frequencyPoints[i-1]) {
//				throw new DbHtException("Frequency points are not in ascending order");
//			}
//		}
//		
//		/**
//		 * work out the most sensitive bin.
//		 */
//		double lowestThresh = Double.MAX_VALUE;
//		for (int i = 0; i < n; i++) {
//			lowestThresh = Math.min(lowestThresh, hearingThreshold[i]);
//		}
//		/**
//		 * See if we need to pack out values out so that they begin and end in 0
//		 */
//		if (frequencyPoints[0] != 0) {
//			n++;
//		}
//		if (frequencyPoints[frequencyPoints.length-1] != niquist) {
//			n++;
//		}
//		filterFrequencies = new double[n];
//		filterGains = new double[n];
//		int iP = 0;
//		if (frequencyPoints[0] != 0) {
//			filterFrequencies[0] = 0;
//			filterGains[0] = 0;
//			iP++;
//		}
//		for (int i = 0; i < frequencyPoints.length; i++, iP++) {
//			filterFrequencies[iP] = frequencyPoints[i]/niquist;
//			filterGains[iP] = Math.pow(10., (lowestThresh-hearingThreshold[i])/20);
//		}
//		if (frequencyPoints[frequencyPoints.length-1] != niquist) {
//			filterFrequencies[n-1] = 1.0;
//			filterGains[n-1] = 0;
//		}
//		
//		
//		return n;
//	}


	@Override
	public OneBandParameters clone() {
		try {
			OneBandParameters newP =  (OneBandParameters) super.clone();
			// do a full copy of the arrays. 
//			if (filterFrequencies != null) {
//				newP.filterFrequencies = Arrays.copyOf(filterFrequencies, filterFrequencies.length);
//			}
//			if (hearingThreshold != null) {
//				newP.hearingThreshold = Arrays.copyOf(hearingThreshold, hearingThreshold.length);
//			}
			if (filterParams == null) {
				filterParams = new FilterParams();
			}
			if (singlePulseThreshold == 0) {
				singlePulseThreshold = 10;
				maxPulseLength = 10;
			}
			return newP;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

//	/**
//	 * @return the frequencyPoints
//	 */
//	public synchronized double[] getFrequencyPoints() {
//		return frequencyPoints;
//	}
//
//	/**
//	 * @param frequencyPoints the frequencyPoints to set
//	 */
//	public synchronized void setFrequencyPoints(double[] frequencyPoints) {
//		this.frequencyPoints = frequencyPoints;
//	}

	public FilterParams getFilterParams() {
//		if (filterParams == null) {
//			filterParams = new FilterParams();
//			if (hearingThreshold != null && hearingThreshold != null) {
//				filterParams.chebyGamma = chebyGamma;
//				filterParams.filterType = FilterType.FIRARBITRARY;
//				filterParams.filterOrder = filterLogOrder;
//				double[] g = new double[hearingThreshold.length];
//				for (int i = 0; i < g.length; i++) {
//					g[i] = -hearingThreshold[i];
//				}
//				filterParams.setArbFilterShape(frequencyPoints, g);
//				filterParams.lastImportFile = lastImportFile;
//			}
//		}
		return filterParams;
	}


	public void setFilterParams(FilterParams filterParams) {
		this.filterParams = filterParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("filterParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return filterParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
}
