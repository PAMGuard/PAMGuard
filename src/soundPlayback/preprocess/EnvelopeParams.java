package soundPlayback.preprocess;

import java.io.Serializable;

import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class EnvelopeParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Ratio enveloped data are mixed back into raw data. 
	 * 0 = raw data only
	 * 1 = mixed data only. 
	 */
	private double mixRatio = 0;
	
	private FilterParams firstFilter;
	
	private FilterParams secondFilter;

	public EnvelopeParams() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected EnvelopeParams clone() {
		try {
			return (EnvelopeParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the mixRatio
	 */
	public double getMixRatio() {
		return mixRatio;
	}

	/**
	 * @param mixRatio the mixRatio to set
	 */
	public void setMixRatio(double mixRatio) {
		this.mixRatio = mixRatio;
	}

	/**
	 * @return the firstFilter
	 */
	public FilterParams getFirstFilter() {
		if (firstFilter == null) {
			firstFilter = new FilterParams(FilterType.BUTTERWORTH, FilterBand.HIGHPASS, 0, 10000, 2);
		}
		return firstFilter;
	}

	/**
	 * @param firstFilter the firstFilter to set
	 */
	public void setFirstFilter(FilterParams firstFilter) {
		this.firstFilter = firstFilter;
	}

	/**
	 * @return the secondFilter
	 */
	public FilterParams getSecondFilter() {
		if (secondFilter == null) {
			secondFilter = new FilterParams(FilterType.BUTTERWORTH, FilterBand.LOWPASS, 10000, 10000, 2);
		}
		return secondFilter;
	}

	/**
	 * @param secondFilter the secondFilter to set
	 */
	public void setSecondFilter(FilterParams secondFilter) {
		this.secondFilter = secondFilter;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}


}
