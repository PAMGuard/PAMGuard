package Localiser;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import fftFilter.FFTFilterParams;

/**
 * Generic parameters associated with delay measurement. 
 * @author Doug Gillespie
 *
 */
public class DelayMeasurementParams implements Serializable, Cloneable, ManagedParameters {


	public static final long serialVersionUID = 1L;

	/**
	 * Filter data prior to bearing measurement
	 */
	public boolean filterBearings;
	
	/**
	 * Parameters for bearing filter 
	 */
	public FFTFilterParams delayFilterParams;
	
	/**
	 * Measure bearings from the waveform envelope, not the full wavefrom. 
	 */
	public boolean envelopeBearings;

	/**
	 * When using envelope, just take the leading edge.
	 */
	public boolean useLeadingEdge;
	
	/*
	 * this is not currently a user parameter but is set by the system based on roughly
	 * where it things the leading edge of the pulse should be. 
	 */
	public int[] leadingEdgeSearchRegion = null;
	
	/**
	 * Waveform upsampling for correlation measurements.
	 * Will only put an option for 1 or 2, but leave generic for the future ...
	 */
	private int upSample = 1;
	
	/**
	 * Restrict the number of bins from sample 0 to use- helps with detection snippets that contain echoes
	 */
	public boolean useRestrictedBins = false; 
	
	/**
	 * The number of restrcited bins to use. 
	 */
	public int restrictedBins=80; 
	
	public DelayMeasurementParams upSample(int upFactor) {
		DelayMeasurementParams newP = this.clone();
//		if (newP.delayFilterParams != null) {
//			newP.delayFilterParams.highPassFreq *= upFactor;
//			newP.delayFilterParams.lowPassFreq *= upFactor;
//		}
		if (newP.leadingEdgeSearchRegion != null) {
			for (int i = 0; i < newP.leadingEdgeSearchRegion.length; i++) {
				newP.leadingEdgeSearchRegion[i] *= upFactor;
			}
		}
		return newP;
	}

	@Override
	public DelayMeasurementParams clone() {
		try {
			DelayMeasurementParams newP = (DelayMeasurementParams) super.clone();
			if (newP.delayFilterParams != null) {
				newP.delayFilterParams = newP.delayFilterParams.clone();
			}
			if (newP.leadingEdgeSearchRegion != null) {
				newP.leadingEdgeSearchRegion = newP.leadingEdgeSearchRegion.clone();
			}
			return newP;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

	/**
	 * String description of the settings
	 * @return
	 */
	private String getDescription() {
		String desc;
		if (filterBearings && delayFilterParams != null) {
			desc = delayFilterParams.toString();
		}
		else {
			desc = "No filter";
		}
		
		if (envelopeBearings && useLeadingEdge) {
			desc += "; correlate envelope leading edge";
		}
		else if (envelopeBearings) {
			desc += "; correlate envelope";
		}
		else desc += "; correlate waveform";
		
		
		if (upSample>1) {
			desc+=("; upsample wave by x" + this.upSample);
		}

		
		if (useRestrictedBins) {
			desc+=("; use only sample 0 to " + this.restrictedBins);
		}
		return desc;
	}

	public void setFftFilterParams(FFTFilterParams newFiltParams) {
		this.delayFilterParams = newFiltParams;
	}
	
	public FFTFilterParams getFftFilterParams() {
		return delayFilterParams;
	}

	/**
	 * @return the upSample. Never < 1
	 */
	public int getUpSample() {
		return Math.max(upSample, 1);
	}

	/**
	 * 
	 * Set the upsample in multiples of the current sample rate. 
	 * So 3 is ends up with a waveform sampled at 3 times the the sample rate. 
	 * @param upSample the upSample to set
	 */
	public void setUpSample(int upSample) {
		this.upSample = upSample;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
