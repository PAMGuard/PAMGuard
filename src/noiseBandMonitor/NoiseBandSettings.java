package noiseBandMonitor;

import java.io.Serializable;
import java.lang.reflect.Field;

import Filters.FilterType;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class NoiseBandSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String rawDataSource;
	public int channelMap = 1;
	@Deprecated
	private int startDecimation = 0;
	@Deprecated
	private int endDecimation = 8;
	public BandType bandType = BandType.THIRDOCTAVE;
	public FilterType filterType = FilterType.BUTTERWORTH;
	public int iirOrder = 6;
	public int firOrder = 7;
	public double firGamma = 2.5;
	@Deprecated //use min Frequency instead.  
	private int lowBandNumber;
	
	@Deprecated //use max Frequency instead.  
	private int highBandNumber = 30; 
	public int outputIntervalSeconds = 10;
	private double maxFrequency;
	private double minFrequency;
	private double referenceFrequency = 1000;
	
	// a few params for the plot
	public boolean logFreqScale = true;
	public boolean showGrid = true;
	public boolean showDecimators = true;
	private boolean[] showStandard = new boolean[3];
	public int scaleToggleState = 0;
	

	@Override
	protected NoiseBandSettings clone() {
		try {
			return (NoiseBandSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean getShowStandard(int iStandard) {
		if (showStandard == null) {
			showStandard = new boolean[3];
		}
		return showStandard[iStandard];
	}
	
	public void setShowStandard(int iStandard, boolean show) {
		if (showStandard == null) {
			showStandard = new boolean[3];
		}
		showStandard[iStandard] = show;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("showStandard");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return showStandard;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * Maximum upper frequency of highest band
	 * @return the maxFrequency
	 */
	public double getMaxFrequency() {
		if (maxFrequency == 0) {
			maxFrequency = BandData.calcCentreFreq(highBandNumber);
			if (bandType == null) {
				bandType = BandType.THIRDOCTAVE;
			}
			maxFrequency *= Math.sqrt(bandType.getBandRatio()) * 1.01;
		}
		return maxFrequency;
	}

	/**
	 * Minimum lower frequency of lowest band. 
	 * @param maxFrequency the maxFrequency to set
	 */
	public void setMaxFrequency(double maxFrequency) {
		this.maxFrequency = maxFrequency;
	}

	/**
	 * Minimum lower frequency of lowest band. 
	 * @return the minFrequency
	 */
	public double getMinFrequency() {
		if (minFrequency == 0) {
//			minFrequency = BandData.calcCentreFreq(lowBandNumber);
			int nDec = Math.abs(endDecimation-startDecimation);
			minFrequency = getMaxFrequency() / Math.pow(2, nDec+1);
			minFrequency /= bandType.getBandRatio() / 1.02;
		}
		return minFrequency;
	}

	/**
	 * Minimum lower frequency of lowest band. 
	 * @param minFrequency the minFrequency to set
	 */
	public void setMinFrequency(double minFrequency) {
		this.minFrequency = minFrequency;
	}

	/**
	 * Frequency to calculate band centres relative to. Default is 1000 Hz. 
	 * @return the referenceFrequency
	 */
	public double getReferenceFrequency() {
		if (referenceFrequency <= 0) {
			referenceFrequency = 1000;
		}
		return referenceFrequency;
	}

	/**
	 * Frequency to calculate band centres relative to. Default is 1000 Hz. 
	 * @param referenceFrequency the referenceFrequency to set
	 */
	public void setReferenceFrequency(double referenceFrequency) {
		this.referenceFrequency = referenceFrequency;
	} 


}
