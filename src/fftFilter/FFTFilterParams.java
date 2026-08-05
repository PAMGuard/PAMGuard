package fftFilter;

import java.io.Serializable;

import Filters.FilterBand;
import Filters.FilterParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.FrequencyFormat;

public class FFTFilterParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public FilterBand filterBand = FilterBand.HIGHPASS;

	public double lowPassFreq, highPassFreq;
	
	/**
	 * Constructor from other type of filter params. 
	 * @param fp
	 */
	public FFTFilterParams(FilterParams fp) {
		this.filterBand = fp.filterBand;
		this.lowPassFreq = fp.lowPassFreq;
		this.highPassFreq = fp.highPassFreq;
	}

	public FFTFilterParams() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public FFTFilterParams clone() {
		try {
			return (FFTFilterParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean equals(Object other) {	
		if (other == null) return false;
		if (!FFTFilterParams.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		FFTFilterParams that = (FFTFilterParams) other;
		return (this.lowPassFreq == that.lowPassFreq & 
				this.highPassFreq == that.highPassFreq &
				this.filterBand == that.filterBand);
	}


	@Override
	public String toString() {
		double[] f = new double[2];
		f[0] = Math.min(highPassFreq, lowPassFreq);
		f[1] = Math.max(highPassFreq, lowPassFreq);
		switch(filterBand) {
		
		case HIGHPASS:
			return String.format("High pass filter %s", FrequencyFormat.formatFrequency(highPassFreq, true));
		case LOWPASS:
			return String.format("Low pass filter %s", FrequencyFormat.formatFrequency(lowPassFreq, true));
		case BANDPASS:
			return String.format("Band pass filter %s", FrequencyFormat.formatFrequencyRange(f, true));
		case BANDSTOP:
			return String.format("Band stop filter %s", FrequencyFormat.formatFrequencyRange(f, true));
		}
		return super.toString();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
